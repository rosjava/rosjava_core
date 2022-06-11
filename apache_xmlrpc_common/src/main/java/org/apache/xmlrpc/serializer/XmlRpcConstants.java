package org.apache.xmlrpc.serializer;

/**
 * Created at 2022-06-11 on 21:51
 *
 * @author Spyros Koukas
 */
public abstract class XmlRpcConstants {
    /**
     * The namespace URI for proprietary XML-RPC extensions.
     */
    public static final String EXTENSIONS_URI = "http://ws.apache.org/xmlrpc/namespaces/extensions";
    public static final String FAULT = "fault";
    public static final String METHOD_RESPONSE = "methodResponse";
    public static final String PARAMS = "params";
    public static final String PARAM = "param";
    public static final String EMPTY_STRING = "";
    public static final String METHOD_CALL = "methodCall";
    public static final String METHOD_NAME = "methodName";
    public static final String EX = "ex";
    public static final String VALUE = "value";
    public static final String FAULT_CODE = "faultCode";
    public static final String FAULT_STRING = "faultString";
    public static final String FAULT_CAUSE = "faultCause";
    /**
     * Tag name of a base64 value.
     */
    public static final String BASE_64 = "base64";
    /**
     * Tag name of a base64 value.
     */
    public static final String SERIALIZABLE = "serializable";
    /** Tag name of an arrays data.
     */
    public static final String DATA = "data";
    /** Tag name of an array value.
     */
    public static final String ARRAY = "array";
    static final String EX_SERIALIZABLE = EX + ":" + SERIALIZABLE;

    private XmlRpcConstants() {
        throw new UnsupportedOperationException("Not for implementation");
    }
}
