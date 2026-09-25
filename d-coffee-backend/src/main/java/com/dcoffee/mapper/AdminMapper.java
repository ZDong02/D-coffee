package com.dcoffee.mapper;

import com.dcoffee.vo.AdminLoginView;
import org.apache.ibatis.annotations.Param;

public interface AdminMapper {
    AdminLoginRecord findActiveAdmin(@Param("username") String username);
    int updateLastLogin(@Param("id") long id);

    class AdminLoginRecord {
        private long id;
        private String username;
        private String passwordHash;
        private String displayName;
        private String role;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
