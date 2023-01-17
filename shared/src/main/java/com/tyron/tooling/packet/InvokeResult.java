package com.tyron.tooling.packet;


import java.util.List;

public class InvokeResult extends Packet {

    private static final String TYPE = InvokeResult.class.getName();

    // if the result is not a primitive, these are the classes
    // that the proxy is going to implement
    private List<String> resultInterfaces;

    // the reference object id to be used when calling methods from this object
    private String resultObjectId;

    private String className;

    public InvokeResult() {
        super(TYPE, null);
    }

    public InvokeResult(String className, List<String> resultInterfaces, String resultObjectId, String packetId) {
        super(TYPE, packetId);
        this.className = className;
        this.resultInterfaces = resultInterfaces;
        this.resultObjectId = resultObjectId;
    }


    public String getResultObjectId() {
        return resultObjectId;
    }

    public List<String> getResultInterfaces() {
        return resultInterfaces;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "InvokeResult{" +
                "resultInterfaces=" + resultInterfaces +
                ", resultObjectId='" + resultObjectId + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
