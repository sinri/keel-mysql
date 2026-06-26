package io.github.sinri.keel.integration.mysql.connection;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLServerVersionMixinTest {

    @Test
    void parseMajorMinorShouldReadFirstTwoNumericSegments() {
        TestVersionMixin mixin = new TestVersionMixin();

        mixin.setMysqlVersion("8.0.35");
        assertEquals(new MySQLServerVersionMixin.MajorMinorVersion(8, 0), mixin.parseMajorMinor());

        mixin.setMysqlVersion("8.1.0");
        assertEquals(new MySQLServerVersionMixin.MajorMinorVersion(8, 1), mixin.parseMajorMinor());

        mixin.setMysqlVersion("9.2.1-commercial");
        assertEquals(new MySQLServerVersionMixin.MajorMinorVersion(9, 2), mixin.parseMajorMinor());

        mixin.setMysqlVersion("8.0.35-0ubuntu0.22.04.1");
        assertEquals(new MySQLServerVersionMixin.MajorMinorVersion(8, 0), mixin.parseMajorMinor());
    }

    @Test
    void isMySQLVersionAtLeastShouldCompareMajorThenMinor() {
        TestVersionMixin mixin = new TestVersionMixin();

        mixin.setMysqlVersion("8.1.0");
        assertTrue(mixin.isMySQLVersionAtLeast(8, 0));
        assertTrue(mixin.isMySQLVersionAtLeast(8, 1));
        assertFalse(mixin.isMySQLVersionAtLeast(8, 2));
        assertFalse(mixin.isMySQLVersionAtLeast(9, 0));

        mixin.setMysqlVersion("9.0.0");
        assertTrue(mixin.isMySQLVersionAtLeast(8, 3));
        assertTrue(mixin.isMySQLVersionAtLeast(9, 0));
    }

    @Test
    void fixedVersionHelpersShouldDelegateToParsedMajorMinor() {
        TestVersionMixin mixin = new TestVersionMixin();

        mixin.setMysqlVersion("5.6.51-log");
        assertTrue(mixin.isMySQLVersion5dot6());
        assertFalse(mixin.isMySQLVersion5dot7());

        mixin.setMysqlVersion("8.2.0");
        assertTrue(mixin.isMySQLVersion8dot2());
        assertFalse(mixin.isMySQLVersion8dot0());
    }

    @Test
    void parseMajorMinorShouldRejectMissingOrInvalidVersion() {
        TestVersionMixin mixin = new TestVersionMixin();

        assertThrows(NullPointerException.class, mixin::parseMajorMinor);

        mixin.setMysqlVersion("8");
        assertThrows(IllegalArgumentException.class, mixin::parseMajorMinor);

        mixin.setMysqlVersion("8.x.0");
        assertThrows(IllegalArgumentException.class, mixin::parseMajorMinor);
    }

    private static class TestVersionMixin implements MySQLServerVersionMixin {
        private @Nullable String mysqlVersion;
        private @Nullable String mysqlSqlMode;
        private @Nullable String mysqlCharacterSetConnection;

        @Override
        public @Nullable String getMysqlVersion() {
            return mysqlVersion;
        }

        @Override
        public void setMysqlVersion(@Nullable String mysqlVersion) {
            this.mysqlVersion = mysqlVersion;
        }

        @Override
        public @Nullable String getMysqlSqlMode() {
            return mysqlSqlMode;
        }

        @Override
        public void setMysqlSqlMode(@Nullable String mysqlSqlMode) {
            this.mysqlSqlMode = mysqlSqlMode;
        }

        @Override
        public @Nullable String getMysqlCharacterSetConnection() {
            return mysqlCharacterSetConnection;
        }

        @Override
        public void setMysqlCharacterSetConnection(@Nullable String mysqlCharacterSetConnection) {
            this.mysqlCharacterSetConnection = mysqlCharacterSetConnection;
        }
    }
}
