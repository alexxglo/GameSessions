package com.playtika.gamesessions.security.models;

public enum RoleType  {
    ROLE_ADMIN(10), ROLE_MANAGER(5), ROLE_USER(0);

    private int roleLevel;
    private RoleType(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public static RoleType stringToRoleType(String role) {
        if(ROLE_ADMIN.toString().equals(role)) {
            return ROLE_ADMIN;
        }
        if(ROLE_MANAGER.toString().equals(role)) {
            return ROLE_MANAGER;
        }
        return ROLE_USER;
    }

    public static String RoleTypeToString(RoleType role) {
        if(ROLE_ADMIN.toString().equals(role.toString())) {
            return "ROLE_ADMIN";
        }
        if(ROLE_MANAGER.toString().equals(role.toString())) {
            return "ROLE_MANAGER";
        }
        return "ROLE_USER";
    }
}
