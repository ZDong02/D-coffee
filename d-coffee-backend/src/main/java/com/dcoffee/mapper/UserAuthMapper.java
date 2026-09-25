package com.dcoffee.mapper;

import org.apache.ibatis.annotations.Param;

public interface UserAuthMapper {
    UserRecord findByPhone(@Param("phone") String phone);
    int insertUser(@Param("phone") String phone, @Param("passwordHash") String passwordHash,
                   @Param("nickname") String nickname);

    class UserRecord {
        private long id;
        private String phone;
        private String passwordHash;
        private String nickname;
        private String status;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
