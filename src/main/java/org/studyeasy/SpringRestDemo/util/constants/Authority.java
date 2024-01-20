package org.studyeasy.SpringRestDemo.util.constants;

public enum Authority {
    USER, // Can update delete self object, read anything
    ADMIN // Can read update delete any object
}
