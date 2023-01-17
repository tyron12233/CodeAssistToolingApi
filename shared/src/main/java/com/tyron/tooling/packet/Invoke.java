package com.tyron.tooling.packet;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Instructs to invoke a method from the server.
 *
 * Each Invoke packet must contain a proper objectId if it is going to invoke
 * a method within the context of the object, if the objectId is null then it is
 * assumed to be a static call.
 */
public class Invoke extends Packet {

    public List<ArgumentHolder> getArguments() {
        return arguments;
    }

    public void setArguments(List<ArgumentHolder> arguments) {
        this.arguments = arguments;
    }

    private static final String TYPE = Invoke.class.getName();

    private String containingClass;
    private String methodName;
    private String objectId;

    private List<ArgumentHolder> arguments;

    public Invoke() {
        super(TYPE, null);
    }

    public Invoke(String objectId, String containingClass, String methodName, String packetId) {
        this(objectId, containingClass, methodName, Collections.emptyList(), packetId);
    }

    public Invoke(String objectId, String containingClass, String methodName, List<ArgumentHolder> arguments, String packetId) {
        super(TYPE, packetId);
        this.objectId = objectId;
        this.containingClass = containingClass;
        this.methodName = methodName;
        this.arguments = arguments;
    }


    public String getContainingClass() {
        return containingClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getObjectId() {
        return objectId;
    }

    @Override
    public String toString() {
        return "Invoke{" +
                "containingClass='" + containingClass + '\'' +
                ", methodName='" + methodName + '\'' +
                ", objectId='" + objectId + '\'' +
                '}';
    }


    /**
     * Serialized argument form, only serializable objects are allowed or objects that are created remotely.
     * e.g. proxied classes
     */
    public static class ArgumentHolder {

        private String className;

        private String objectId;

        private Serializable value;

        public ArgumentHolder() {

        }

        public ArgumentHolder(String className, String objectId, Serializable value) {
            this.className = className;
            this.objectId = objectId;
            this.value = value;
        }

        public Serializable getValue() {
            return value;
        }

        public String getObjectId() {
            return objectId;
        }

        public String getClassName() {
            return className;
        }
    }
}
