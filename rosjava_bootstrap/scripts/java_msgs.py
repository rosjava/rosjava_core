#!/usr/bin/env python
# Software License Agreement (BSD License)
#
# Copyright (c) 2009, Willow Garage, Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#  * Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#  * Redistributions in binary form must reproduce the above
#    copyright notice, this list of conditions and the following
#    disclaimer in the documentation and/or other materials provided
#    with the distribution.
#  * Neither the name of Willow Garage, Inc. nor the names of its
#    contributors may be used to endorse or promote products derived
#    from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
# FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#

# Authors:
#  - Lorenz Moesenlechner (moesenle@in.tum.de)
#  - Jason Wolfe (jawolfe@willowgarage.com)
#  - Nicholas Butko (nbutko@cogsci.ucsd.edu)
#  - Ken Conley (kwc@willowgarage.com)

from __future__ import with_statement

import roslib; roslib.load_manifest('rosjava')

import sys
import os
import traceback

import roslib.msgs 
import roslib.packages
import roslib.gentools

from cStringIO import StringIO

# script name
NAME="java_msgs.py"

# top-level java package namespace to generate messages into
JAVA_PACKAGE = 'org.ros.message.'
# name of Header message class
HEADER = '%sstd_msgs.Header'%JAVA_PACKAGE
# name of Message base class
MESSAGE_CLASS = 'org.ros.message.Message'

# we special case uint8 to always be byte. The user must use conversion
# utilities to convert from signed to unsigned representation.
MSG_TYPE_TO_JAVA = {'bool': 'boolean',
                    'char': 'byte',
                    'byte': 'byte',
                    'uint8': 'byte', 'int8': 'byte', 
                    'uint16': 'int', 'int16': 'short', 
                    'uint32': 'long', 'int32': 'int',
                    'uint64': 'long', 'int64': 'long',
                    'float32': 'float',
                    'float64': 'double',
                    'string': 'java.lang.String',
                    'time': '%sTime'%JAVA_PACKAGE,
                    'duration': '%sDuration'%JAVA_PACKAGE}

MSG_TYPE_TO_SERIALIZATION_CODE = {
    'bool': '%(buffer)s.put((byte)(%(name)s ? 1 : 0))',
    'char': '%(buffer)s.put(%(name)s)',
    'byte': '%(buffer)s.put(%(name)s)',
    'uint8': '%(buffer)s.put(%(name)s)',
    'int8': '%(buffer)s.put(%(name)s)',
    'uint16': '%(buffer)s.putShort((short)%(name)s)',
    'int16': '%(buffer)s.putShort(%(name)s)',
    'uint32': '%(buffer)s.putInt((int)%(name)s)',
    'int32': '%(buffer)s.putInt(%(name)s)',
    'uint64': '%(buffer)s.putLong(%(name)s)',
    'int64': '%(buffer)s.putLong(%(name)s)',
    'float32': '%(buffer)s.putFloat(%(name)s)',
    'float64': '%(buffer)s.putDouble(%(name)s)',
    'string': 'Serialization.writeString(%(buffer)s, %(name)s)',
    'time': 'Serialization.writeTime(%(buffer)s, %(name)s)',
    'duration': 'Serialization.writeDuration(%(buffer)s, %(name)s)'}

MSG_TYPE_TO_DESERIALIZATION_CODE = {
    'bool': '%s.get() != 0 ? true : false',
    'char': '%s.get()',
    'byte': '%s.get()',
    'uint8': '%s.get()',
    'int8': '%s.get()',
    'uint16': '(int)(%s.getShort() & 0xffff)',
    'int16': '%s.getShort()',
    'uint32': '(long)(%s.getInt() & 0xffffffff)',
    'int32': '%s.getInt()',
    'uint64': '%s.getLong()',
    'int64': '%s.getLong()',
    'float32': '%s.getFloat()',
    'float64': '%s.getDouble()',
    'string': 'Serialization.readString(%s)',
    'time': 'Serialization.readTime(%s)',
    'duration': 'Serialization.readDuration(%s)'}

BUILTIN_TYPE_SIZES = {'bool': 1, 'int8': 1, 'byte': 1, 'int16': 2, 'int32': 4, 'int64': 8,
                      'char': 1, 'uint8': 1, 'uint16': 2, 'uint32': 4, 'uint64': 8,
                      'float32': 4, 'float64': 8, 'time': 8, 'duration': 8}

JAVA_PRIMITIVE_TYPES = ['char', 'byte', 'short', 'int', 'long', 'boolean', 'float', 'double']

JAVA_HASH_CODES = {
    'char': '%(value)s',
    'byte': '%(value)s',
    'short': '%(value)s',
    'int': '%(value)s',
    'long': '(int)(%(value)s ^ (%(value)s >>> 32))',
    'boolean': '(%(value)s ? 1231 : 1237)',     # stolen from eclipse autogenerated code
    'float': 'Float.floatToIntBits(%(value)s)',
    'double': '(int)((tmp = Double.doubleToLongBits(%(value)s)) ^ (tmp >>> 32))'}

def builtin_type_size(type):
    return BUILTIN_TYPE_SIZES[type.split('[')[0]]

def base_type_to_java(base_type):
    base_type = base_type.split('[')[0]
    if (roslib.msgs.is_builtin(base_type)):
        java_type = MSG_TYPE_TO_JAVA[base_type]
    elif (len(base_type.split('/')) == 1):
        if (roslib.msgs.is_header_type(base_type)):
            java_type = HEADER
        else:
            java_type = base_type
    else:
        pkg = base_type.split('/')[0]
        msg = base_type.split('/')[1]
        java_type = '%s%s.%s' % (JAVA_PACKAGE, pkg, msg)
    return java_type

def base_type_serialization_code(type):
    return MSG_TYPE_TO_SERIALIZATION_CODE[type.split('[')[0]]

def base_type_deserialization_code(type):
    return MSG_TYPE_TO_DESERIALIZATION_CODE[type.split('[')[0]]

def type_initializer(type, default_val = None):
    if default_val is not None:
        return ' = %s' % default_val
    elif roslib.msgs.is_builtin(type):
        if type in ['time', 'duration', 'string']:
            return ' = new %s()' % base_type_to_java(type)
        else:
            return ''
    else:
        return ' = new %s()' % base_type_to_java(type)
    
def msg_decl_to_java(field, default_val=None):
    """
    Converts a message type (e.g. uint32, std_msgs/String, etc.) into the Java declaration
    for that type.
    
    @param type: The message type
    @type type: str
    @return: The Java declaration
    @rtype: str
    """
    java_type = base_type_to_java(field.type)

    if type(field).__name__ == 'Field' and field.is_array:
        if field.array_len is None:
            if field.is_builtin and java_type in JAVA_PRIMITIVE_TYPES:
                decl_string = '%(java_type)s[] %(name)s = new %(java_type)s[0]'
            else:
                decl_string = 'java.util.ArrayList<%(java_type)s> %(name)s = new java.util.ArrayList<%(java_type)s>()'
            return decl_string % { 'name': field.name, 'java_type': java_type}
        else:
            return '%(java_type)s[] %(name)s = new %(java_type)s[%(array_len)d]' % {'java_type': java_type,
                                                                                    'name': field.name,
                                                                                    'array_len': field.array_len}
    else:
        return '%(type)s %(name)s%(initializer)s' % {'type': java_type,
                                                     'name': field.name,
                                                     'initializer': type_initializer(field.type, default_val)}
    
def write_begin(s, spec, file):
    """
    Writes the beginning of the header file: a comment saying it's auto-generated and the include guards
    
    @param s: The stream to write to
    @type s: stream
    @param spec: The spec
    @type spec: roslib.msgs.MsgSpec
    @param file: The file this message is being generated for
    @type file: str
    """
    s.write('/* Auto-generated by genmsg_java.py for file %s */\n'%(file))
    s.write('\npackage %s%s;\n' % (JAVA_PACKAGE, spec.package))
    s.write('\nimport java.nio.ByteBuffer;\n')
    
def write_end(s, spec):
    """
    Writes the end of the header file: the ending of the include guards
    
    @param s: The stream to write to
    @type s: stream
    @param spec: The spec
    @type spec: roslib.msgs.MsgSpec
    """
    pass
    
def write_imports(s, spec):
    """
    Writes the message-specific imports
    
    @param s: The stream to write to
    @type s: stream
    @param spec: The message spec to iterate over
    @type spec: roslib.msgs.MsgSpec
    """
    s.write('\n') 
    
    
def write_class(s, spec, extra_metadata_methods={}, static=False):
    """
    Writes the entire message struct: declaration, constructors, members, constants and member functions
    @param s: The stream to write to
    @type s: stream
    @param spec: The message spec
    @type spec: roslib.msgs.MsgSpec
    """
    
    msg = spec.short_name
    if static:
        s.write('static public class %s extends %s {\n' % (msg, MESSAGE_CLASS))
    else:
        s.write('public class %s extends %s {\n' % (msg, MESSAGE_CLASS))

    write_constant_declarations(s, spec)
    write_members(s, spec)

    write_constructor(s, spec)
    
    gendeps_dict = roslib.gentools.get_dependencies(spec, spec.package, compute_files=False)
    md5sum = roslib.gentools.compute_md5(gendeps_dict)
    full_text = compute_full_text_escaped(gendeps_dict)
    
    write_member_functions(s, spec,
                           dict({'MD5Sum': '"%s"' % md5sum,
                                 'DataType': '"%s/%s"' % (spec.package, spec.short_name),
                                 'MessageDefinition': full_text},
                                **extra_metadata_methods))
    
    s.write('} // class %s\n'%(msg))

def write_constructor(s, spec):
    s.write("""
  public %s() {
""" % spec.short_name)
    for field in spec.parsed_fields():
        if field.type.split('[')[0] in roslib.msgs.PRIMITIVE_TYPES and  \
                field.type.split('[')[0] != 'string':
            continue
        if field.is_array and field.array_len:
            s.write("""
    for(int __i=0; __i<%(array_len)d; __i++) {
      %(name)s[__i]%(initializer)s;
    }
""" % {'name': field.name, 'type': field.type.split('[')[0],
       'array_len': field.array_len,
       'initializer': type_initializer(field.type.split('[')[0])})
    s.write('  }\n')
            
def write_member(s, field):
    """
    Writes a single member's declaration and type typedef
    
    @param s: The stream to write to
    @type s: stream
    @param type: The member type
    @type type: str
    @param name: The name of the member
    @type name: str
    """
    java_decl = msg_decl_to_java(field)
    s.write('  public %s;\n' % java_decl)

def write_members(s, spec):
    """
    Write all the member declarations
    
    @param s: The stream to write to
    @type s: stream
    @param spec: The message spec
    @type spec: roslib.msgs.MsgSpec
    """
    [write_member(s, field) for field in spec.parsed_fields()]
        
def escape_string(str):
    str = str.replace('\\', '\\\\')
    str = str.replace('"', '\\"')
    return str
        
def write_constant_declaration(s, constant):
    """
    Write a constant value as a static member
    
    @param s: The stream to write to
    @type s: stream
    @param constant: The constant
    @type constant: roslib.msgs.Constant
    """
    if constant.type == 'string':
        s.write('  static public final %s;\n'% msg_decl_to_java(constant, '"' + escape_string(constant.val) + '"'))
    else:
        s.write('  static public final %s;\n'% msg_decl_to_java(constant, '(' + base_type_to_java(constant.type) + ')' + str(constant.val)))
        
def write_constant_declarations(s, spec):
    """
    Write all the constants from a spec as static members
    
    @param s: The stream to write to
    @type s: stream
    @param spec: The message spec
    @type spec: roslib.msgs.MsgSpec
    """
    for constant in spec.constants:
        write_constant_declaration(s, constant) 
    s.write('\n')
    
def write_clone_methods(s, spec):
    s.write("""
  @Override
  public %(type)s clone() {
    %(type)s c = new %(type)s();
    c.deserialize(serialize(0));
    return c;
  }
""" % {'type': spec.short_name})
    s.write("""
  @Override
  public void setTo(%s m) {
    deserialize(m.serialize(0));
  }
"""%(MESSAGE_CLASS))

def write_serialization_length(s, spec):
    s.write("""
  @Override
  public int serializationLength() {
    int __l = 0;
""")
    for field in spec.parsed_fields():
        java_type = base_type_to_java(field.base_type)
        if field.type.split('[')[0] == 'string':
            if field.is_array:
                if field.array_len is None:
                    s.write('    __l += 4;')
                s.write("""
    for(java.lang.String val : %(name)s) {
      __l += 4 + val.length();
    }
""" % {'name': field.name})
            else:
                s.write('    __l += 4 + %s.length();\n' % field.name)

        elif field.is_builtin:
            if field.is_array and field.array_len is None:
                if java_type in JAVA_PRIMITIVE_TYPES:
                    size_expr = '4 + %s.length * %d' % (field.name, builtin_type_size(field.type))
                else:
                    size_expr = '4 + %s.size() * %d' % (field.name, builtin_type_size(field.type))
            elif field.is_array:
                size_expr = '%d' % (int(field.array_len) * builtin_type_size(field.type))
            else:
                size_expr = '%d' % builtin_type_size(field.type)
            s.write('    __l += %s; // %s\n' % (size_expr, field.name))
        elif field.is_array:
            if field.array_len is None:
                s.write('    __l += 4;')
            s.write("""
    for(%s val : %s) {
      __l += val.serializationLength();
    }
""" % (java_type, field.name))
        else:
            s.write('    __l += %s.serializationLength();\n' % field.name)
                        
    s.write('    return __l;\n  }\n')

def write_serialization_method(s, spec):
    s.write("""
  @Override
  public void serialize(ByteBuffer bb, int seq) {
""")
    for field in spec.parsed_fields():
        java_type = base_type_to_java(field.base_type)
        if field.is_builtin:
            if field.is_array:
                if field.array_len is None:
                    if java_type in JAVA_PRIMITIVE_TYPES:
                        s.write('    bb.putInt(%s.length);' % field.name)
                    else:
                        s.write('    bb.putInt(%s.size());' % field.name)
                s.write("""
    for(%(type)s val : %(name)s) {
      %(serialization)s;
    }
""" % {'type': java_type,
       'name': field.name,
       'serialization': base_type_serialization_code(field.type) % {'buffer': 'bb', 'name': 'val'}})

            # No array. Use primitive serialization
            else:
                s.write('    %s;\n' % (base_type_serialization_code(field.type) % {'buffer': 'bb',
                                                                                   'name': field.name}))
        # Not a builtin type, but array
        else:
            if field.is_array:
                if field.array_len is None:
                    s.write('    bb.putInt(%s.size());' % field.name)
                s.write("""
    for(%s val : %s) {
      val.serialize(bb, seq);
    }
""" % (java_type, field.name))

            # No primitive type, no array
            else:
                s.write('    %s.serialize(bb, seq);\n' % field.name)
    
    s.write('  }\n')

def write_deserialization_method(s, spec):
    s.write("""
  @Override
  public void deserialize(ByteBuffer bb) {
""")
    for field in spec.parsed_fields():
        java_type = base_type_to_java(field.base_type)

        if field.is_array:
            # Template fields:
            # size_initializer
            # type_initializer
            # deserialization code

            size_initializer = None
            type_initializer = None
            deserialization_code = None

            if field.array_len is None:
                size_initializer = 'bb.getInt()'
                if java_type not in JAVA_PRIMITIVE_TYPES:
                    type_initializer = 'new java.util.ArrayList<%(type)s>(__%(name)s_len)'
                    if field.is_builtin:
                        deserialization_code = '%(name)s.add(%(deserialization_code)s)' \
                            % {'name': '%(name)s',
                               'deserialization_code': base_type_deserialization_code(field.type) % 'bb'}
                    else:
                        deserialization_code = """%(type)s __tmp = new %(type)s();
%(indent)s__tmp.deserialize(bb);
%(indent)s%(name)s.add(__tmp);"""

            if not size_initializer:
                size_initializer = '%(name)s.length;' % {'name': field.name}
            if not type_initializer:
                type_initializer = 'new %(type)s[__%(name)s_len]'
            if not deserialization_code:
                if field.is_builtin:
                    deserialization_code = '%(name)s[__i] = %(deserialization_code)s' \
                        % {'name': '%(name)s',
                           'deserialization_code': base_type_deserialization_code(field.type) % 'bb'}
                else:
                    deserialization_code = """%(type)s __tmp = new %(type)s();
%(indent)s__tmp.deserialize(bb);
%(indent)s%(name)s[__i] = __tmp"""

            # Assemble the code from size_initializer, type_initializer and deserialization_code
            default_vars_dict = {'name': field.name, 'type': java_type}
            s.write("""
    int __%(name)s_len = %(size_initializer)s;
    %(name)s = %(type_initializer)s;
    for(int __i=0; __i<__%(name)s_len; __i++) {
      %(deserialization_code)s;
    }
""" % dict(default_vars_dict,
           **{'size_initializer': size_initializer % default_vars_dict,
              'type_initializer': type_initializer % default_vars_dict,
              'deserialization_code': deserialization_code % dict(default_vars_dict, **{'indent': 6*' '})}))

        # No array. Default deserialization.
        elif field.is_builtin:
            s.write('    %s = %s;\n' % (field.name,
                                        base_type_deserialization_code(field.type) % 'bb'))
        else:
            s.write('    %s.deserialize(bb);\n' % field.name)
    s.write('  }\n')
    
def write_serialization_methods(s, spec):
    write_serialization_length(s, spec)
    write_serialization_method(s, spec)
    write_deserialization_method(s, spec)
    write_compare_methods(s, spec)

def write_msg_metadata_method(s, name, return_value):
    s.write('  public static java.lang.String __s_get%s() { return %s; }\n' % (name, return_value))
    s.write('  @Override')
    s.write('  public java.lang.String get%(name)s() { return __s_get%(name)s(); }\n'
            % {'name': name})

def write_equals_method(s, spec):
    s.write("""
  @SuppressWarnings("all")
  public boolean equals(Object o) {
    if(!(o instanceof %(type)s))
      return false;
    %(type)s other = (%(type)s) o;
    return
""" % {'type': spec.short_name})

    for field in spec.parsed_fields():
        java_type = base_type_to_java(field.base_type)
        template_dict = {'name': field.name}
        if field.is_array and (field.array_len or java_type in JAVA_PRIMITIVE_TYPES):
            s.write('      java.util.Arrays.equals(%(name)s, other.%(name)s) &&\n' % template_dict)
        elif not field.is_array and java_type in JAVA_PRIMITIVE_TYPES:
            s.write('      %(name)s == other.%(name)s &&\n' % template_dict)
        else:
            s.write('      %(name)s.equals(other.%(name)s) &&\n' % template_dict)
    s.write("""      true;
  }
""")

def write_hash_code_method(s, spec):
    s.write("""
  @SuppressWarnings("all")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long tmp;
""")
    for field in spec.parsed_fields():
        java_type = base_type_to_java(field.base_type)
        template_dict = {'name': 'this.%s' % field.name}
        if field.is_array and (field.array_len or java_type in JAVA_PRIMITIVE_TYPES):
            s.write('    result = prime * result + java.util.Arrays.hashCode(%(name)s);\n' % template_dict)
        elif not field.is_array and java_type in JAVA_PRIMITIVE_TYPES:
            s.write('    result = prime * result + %(hash_code)s;\n' \
                        % dict(template_dict, **{'hash_code': JAVA_HASH_CODES[java_type] % {'value': template_dict['name']}}))
        else:
            s.write('    result = prime * result + (%(name)s == null ? 0 : %(name)s.hashCode());\n' % template_dict)
    s.write('    return result;\n  }\n')
    
def write_compare_methods(s, spec):
    write_equals_method(s, spec)
    write_hash_code_method(s, spec)
    
def write_member_functions(s, spec, msg_metadata_methods):
    """
    The the default member functions
    """
    s.write('\n')
    for method_desc in msg_metadata_methods.items():
        write_msg_metadata_method(s, *method_desc)

    write_clone_methods(s, spec)
    write_serialization_methods(s, spec)
    
def compute_full_text_escaped(gen_deps_dict):
    """
    Same as roslib.gentools.compute_full_text, except that the
    resulting text is escaped to be safe for C++ double quotes

    @param get_deps_dict: dictionary returned by get_dependencies call
    @type  get_deps_dict: dict
    @return: concatenated text for msg/srv file and embedded msg/srv types. Text will be escaped for double quotes
    @rtype: str
    """
    definition = roslib.gentools.compute_full_text(gen_deps_dict)
    lines = definition.split('\n')
    s = StringIO()
    for line in lines:
        line = escape_string(line)
        s.write('\"%s\\n\" +\n'%(line))

    s.write('\"\"')
    val = s.getvalue()
    s.close()
    return val

def generate_msg(package, msg_path, output_dir):
    _, spec = roslib.msgs.load_from_file(msg_path, package)
    s = StringIO()
    try:
        write_begin(s, spec, msg_path)
        write_imports(s, spec)

        write_class(s, spec)

        write_end(s, spec)

        if not os.path.exists(output_dir):
            # if we're being run concurrently, the above test can report false but os.makedirs can still fail if
            # another copy just created the directory
            try:
                os.makedirs(output_dir)
            except OSError, e:
                pass

        output_f = os.path.join(output_dir, '%s.java'%(spec.short_name))
        with open(output_f, 'w') as f:
            f.write(s.getvalue())
    finally:
        s.close()
    
def generate(package, output_dir=None):
    """
    Generate all messages for specified package.
    
    @param package: ROS package name to generate messages for 
    @type  package: str
    @param output_dir: (optional) directory to store output in
    @type  output_dir: str
    
    @return: output directory
    @rtype: str
    """
    # org.ros. -> org/ros/
    package_prefix = os.sep.join(JAVA_PACKAGE.split('.'))

    if output_dir is None:
        output_dir = os.path.join('msg_gen', 'java')
    package_output_dir = os.path.join(output_dir, package_prefix, package)

    msgs = roslib.msgs.list_msg_types(package, False)
    for m in msgs:
        msg_path = roslib.msgs.msg_file(package, m)
        generate_msg(package, msg_path, package_output_dir)
    return package_output_dir

def generate_messages(argv):
    from optparse import OptionParser
    parser = OptionParser(usage="usage: %prog [options] <packages...>", prog=NAME)
    parser.add_option("-o", "--outputdir",
                      dest="output_dir", default=None,
                      help="set output directory", metavar="DIRECTORY")
    options, packages = parser.parse_args(argv[1:])
    if not packages:
        usage()
    for p in packages:
        print "generating messages for package [%s]"%(p)
        output_dir = generate(p, output_dir=options.output_dir.strip())
        print "generated messages for package [%s] to [%s]"%(p, output_dir)

if __name__ == "__main__":
    generate_messages(sys.argv)
