/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringEscapeUtils;
import org.ros.exception.RosRuntimeException;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;

import java.util.Set;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageInterfaceBuilder {

  private MessageDeclaration messageDeclaration;
  private String packageName;
  private String interfaceName;
  private boolean addConstantsAndMethods;
  private String nestedContent;

  // TODO(damonkohler): Upgrade Apache Commons Lang. See
  // https://issues.apache.org/jira/browse/LANG-437
  private static String escapeJava(String str) {
    return StringEscapeUtils.escapeJava(str).replace("\\/", "/").replace("'", "\\'");
  }

  public MessageDeclaration getMessageDeclaration() {
    return messageDeclaration;
  }

  public MessageInterfaceBuilder setMessageDeclaration(MessageDeclaration messageDeclaration) {
    Preconditions.checkNotNull(messageDeclaration);
    this.messageDeclaration = messageDeclaration;
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  /**
   * @param packageName
   *          the package name of the interface or {@code null} if no package
   *          name should be specified
   * @return this {@link MessageInterfaceBuilder}
   */
  public MessageInterfaceBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public MessageInterfaceBuilder setInterfaceName(String interfaceName) {
    Preconditions.checkNotNull(interfaceName);
    this.interfaceName = interfaceName;
    return this;
  }

  public boolean getAddConstantsAndMethods() {
    return addConstantsAndMethods;
  }

  public void setAddConstantsAndMethods(boolean enabled) {
    addConstantsAndMethods = enabled;
  }

  public String getNestedContent() {
    return nestedContent;
  }

  public void setNestedContent(String nestedContent) {
    this.nestedContent = nestedContent;
  }

  public String build(MessageFactory messageFactory) {
    Preconditions.checkNotNull(messageDeclaration);
    Preconditions.checkNotNull(interfaceName);
    StringBuilder builder = new StringBuilder();
    if (packageName != null) {
      builder.append(String.format("package %s;\n\n", packageName));
    }
    builder.append(String.format(
        "public interface %s extends org.ros.internal.message.Message {\n", interfaceName));
    builder.append(String.format("  static final java.lang.String _TYPE = \"%s\";\n",
        messageDeclaration.getType()));
    builder.append(String.format("  static final java.lang.String _DEFINITION = \"%s\";\n",
        escapeJava(messageDeclaration.getDefinition())));
    if (addConstantsAndMethods) {
      MessageContextFactory messageContextFactory = new MessageContextFactory(messageFactory);
      MessageContext messageContext =
          messageContextFactory.newFromMessageDeclaration(messageDeclaration);
      appendConstants(messageContext, builder);
      appendSettersAndGetters(messageContext, builder);
    }
    if (nestedContent != null) {
      builder.append("\n");
      builder.append(nestedContent);
    }
    builder.append("}\n");
    return builder.toString();
  }

  @SuppressWarnings("deprecation")
  private String getJavaValue(PrimitiveFieldType primitiveFieldType, String value) {
    switch (primitiveFieldType) {
    case BOOL:
      return Boolean.valueOf(!value.equals("0") && !value.equals("false")).toString();
    case FLOAT32:
      return value + "f";
    case STRING:
      return "\"" + escapeJava(value) + "\"";
    case BYTE:
    case CHAR:
    case INT8:
    case UINT8:
    case INT16:
    case UINT16:
    case INT32:
    case UINT32:
    case INT64:
    case UINT64:
    case FLOAT64:
      return value;
    default:
      throw new RosRuntimeException("Unsupported PrimitiveFieldType: " + primitiveFieldType);
    }
  }

  private void appendConstants(MessageContext messageContext, StringBuilder builder) {
    for (Field field : messageContext.getFields()) {
      if (field.isConstant()) {
        Preconditions.checkState(field.getType() instanceof PrimitiveFieldType);
        PrimitiveFieldType primitiveFieldType = (PrimitiveFieldType) field.getType();
        String value = getJavaValue(primitiveFieldType, field.getValue().toString());
        builder.append(String.format("  static final %s %s = %s;\n",
            primitiveFieldType.getJavaTypeName(), field.getName(), value));
      }
    }
  }

  private void appendSettersAndGetters(MessageContext messageContext, StringBuilder builder) {
    Set<String> setters = Sets.newHashSet();
    Set<String> getters = Sets.newHashSet();
    for (Field field : messageContext.getFields()) {
      if (field.isConstant()) {
        continue;
      }
      String type = field.getJavaTypeName();
      String getter = field.getGetterName();
      if (!getters.contains(getter)) {
        builder.append(String.format("  %s %s();\n", type, getter));
      }
      getters.add(getter);
      String setter = field.getSetterName();
      if (!setters.contains(setter)) {
        builder.append(String.format("  void %s(%s value);\n", setter, type));
      }
      setters.add(setter);
    }
  }
}
