package com.dcoffee.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/** 在本机交互式终端中安全开通首位管理员。 */
public final class AdminProvisioningCli {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{3,64}");

    private AdminProvisioningCli() {
    }

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("请在可交互的 PowerShell 或命令提示符中运行，避免密码回显。");
        }

        Properties properties = loadProperties();
        String driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver").trim();
        String url = resolve(properties.getProperty("db.url"), "DB_URL");
        String username = resolve(properties.getProperty("db.username"), "DB_USERNAME");
        String password = resolve(properties.getProperty("db.password"), "DB_PASSWORD");
        if (url.isBlank() || username.isBlank()) {
            throw new IllegalStateException("数据库地址或账号未配置，请检查环境变量及 application.properties。");
        }

        String adminUsername = console.readLine("管理员登录名（3–64 位英文、数字、点、下划线或短横线）：");
        if (adminUsername == null || !USERNAME_PATTERN.matcher(adminUsername.trim()).matches()) {
            throw new IllegalArgumentException("管理员登录名格式不正确。");
        }
        adminUsername = adminUsername.trim();
        String displayName = console.readLine("显示名称（直接回车使用登录名）：");
        if (displayName == null || displayName.isBlank()) {
            displayName = adminUsername;
        }
        if (displayName.length() > 64) {
            throw new IllegalArgumentException("显示名称不能超过 64 个字符。");
        }

        String rawPassword = readValidatedPassword(console);

        Class.forName(driver);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            connection.setAutoCommit(false);
            try {
                if (hasAdministrator(connection)) {
                    throw new IllegalStateException("系统中已存在管理员；为安全起见，本工具只允许初始化首位管理员。");
                }
                long roleId = ensureAdminRole(connection);
                String passwordHash = new BCryptPasswordEncoder().encode(rawPassword);
                insertAdministrator(connection, adminUsername, displayName, roleId, passwordHash);
                connection.commit();
                console.printf("管理员账号 %s 已创建。密码仅以 BCrypt 摘要保存。%n", adminUsername);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = AdminProvisioningCli.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("找不到 application.properties。");
            }
            properties.load(input);
        }
        return properties;
    }

    private static String readValidatedPassword(Console console) {
        while (true) {
            char[] firstPassword = console.readPassword("管理员密码（至少 12 个字符）：");
            char[] confirmedPassword = console.readPassword("再次输入密码：");
            if (firstPassword == null || confirmedPassword == null) {
                clear(firstPassword, confirmedPassword);
                throw new IllegalStateException("无法读取密码输入。");
            }
            if (!Arrays.equals(firstPassword, confirmedPassword)) {
                clear(firstPassword, confirmedPassword);
                console.printf("两次输入的密码不一致，请重新输入。%n");
                continue;
            }

            String password = new String(firstPassword);
            clear(firstPassword, confirmedPassword);
            int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
            if (password.length() >= 12 && passwordBytes <= 72) {
                return password;
            }
            console.printf("密码至少需要 12 个字符，且 UTF-8 编码不能超过 72 字节，请重新输入。%n");
        }
    }

    private static String resolve(String configuredValue, String environmentKey) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null) {
            return environmentValue;
        }
        if (configuredValue == null) {
            return "";
        }
        String placeholderPrefix = "${" + environmentKey + ":";
        if (configuredValue.startsWith(placeholderPrefix) && configuredValue.endsWith("}")) {
            return configuredValue.substring(placeholderPrefix.length(), configuredValue.length() - 1);
        }
        return configuredValue;
    }

    private static boolean hasAdministrator(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM dc_admin WHERE deleted = 0");
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getLong(1) > 0;
        }
    }

    private static long ensureAdminRole(Connection connection) throws SQLException {
        try (PreparedStatement upsert = connection.prepareStatement(
                "INSERT INTO dc_admin_role (role_code, name, description, status, deleted) " +
                        "VALUES ('ADMIN', '系统管理员', 'D-coffee 管理后台管理员', 'ACTIVE', 0) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), " +
                        "status = 'ACTIVE', deleted = 0")) {
            upsert.executeUpdate();
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT id FROM dc_admin_role WHERE role_code = 'ADMIN' AND status = 'ACTIVE' AND deleted = 0");
             ResultSet result = query.executeQuery()) {
            if (!result.next()) {
                throw new SQLException("无法初始化管理员角色。");
            }
            return result.getLong(1);
        }
    }

    private static void insertAdministrator(Connection connection, String username, String displayName,
                                             long roleId, String passwordHash) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO dc_admin (username, password_hash, display_name, role_id, status, deleted) " +
                        "VALUES (?, ?, ?, ?, 'ACTIVE', 0)")) {
            insert.setString(1, username);
            insert.setString(2, passwordHash);
            insert.setString(3, displayName);
            insert.setLong(4, roleId);
            insert.executeUpdate();
        }
    }

    private static void clear(char[]... values) {
        for (char[] value : values) {
            if (value != null) {
                Arrays.fill(value, '\0');
            }
        }
    }
}
