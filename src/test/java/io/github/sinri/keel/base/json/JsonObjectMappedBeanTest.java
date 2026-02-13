package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 辅助方法：打印格式化的 JSON 输出，用于可视化测试结果
 */
class JsonObjectMappedBeanTest {

    /**
     * 打印格式化的 JSON 输出
     *
     * @param testName 测试名称
     * @param json     JSON 对象
     */
    private void printJsonOutput(String testName, JsonObject json) {
        System.out.println("\n========== " + testName + " ==========");
        System.out.println("Generated JSON:");
        System.out.println(json.encodePrettily());
        System.out.println("Size: " + json.size());
        System.out.println("Keys: " + json.getMap().keySet());
        System.out.println("=====================================\n");
    }

    /**
     * 测试基本的驼峰转蛇形转换
     */
    @Test
    void testBasicCamelCaseToSnakeCase() {
        TestBean bean = new TestBean();
        bean.setUserName("testUser");
        bean.setUserId(123);

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testBasicCamelCaseToSnakeCase", json);

        assertEquals("testUser", json.getString("user_name"));
        assertEquals(123, json.getInteger("user_id"));
    }

    /**
     * 测试 is 开头的 getter 方法（boolean 类型）
     */
    @Test
    void testBooleanGetterWithIsPrefix() {
        TestBean bean = new TestBean();
        bean.setActive(true);

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testBooleanGetterWithIsPrefix", json);

        // isActive 方法以 is 开头，应该被包含在输出中
        assertTrue(json.containsKey("active"));
        assertTrue(json.getBoolean("active"));
        assertFalse(json.containsKey("is_active"));
    }

    /**
     * 测试连续大写字母的正确转换（缩写词）
     */
    @Test
    void testConsecutiveUppercaseLetters() {
        TestBean bean = new TestBean();
        bean.setIpAddress("192.168.1.1");
        bean.setXmlHttpRequest("<request>");
        bean.setHttpResponse("<response>");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testConsecutiveUppercaseLetters", json);

        assertEquals("192.168.1.1", json.getString("ip_address"));
        assertEquals("<request>", json.getString("xml_http_request"));
        assertEquals("<response>", json.getString("http_response"));
    }

    /**
     * 测试方法名为 get 的情况应该被跳过
     */
    @Test
    void testGetMethodShouldBeSkipped() {
        TestBean bean = new TestBean();
        bean.setGet("value");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testGetMethodShouldBeSkipped", json);

        // get() 方法去掉 get 前缀后为空，应该被跳过
        assertFalse(json.containsKey(""));
        assertFalse(json.containsKey("get"));
    }

    /**
     * 测试方法名为 getX 的情况
     */
    @Test
    void testGetXMethod() {
        TestBean bean = new TestBean();
        bean.setGetX("xValue");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testGetXMethod", json);

        assertEquals("xValue", json.getString("x"));
    }

    /**
     * 测试带参数的 getter 方法应该被忽略
     */
    @Test
    void testGetterWithParametersShouldBeIgnored() {
        TestBean bean = new TestBean();
        bean.setUserName("testUser");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testGetterWithParametersShouldBeIgnored", json);

        // getUserName(String prefix) 有参数，应该被忽略
        // 只有无参数的 getUserName() 会被转换
        assertEquals("testUser", json.getString("user_name"));
        // 不应该有带前缀的键
        assertFalse(json.containsKey("user_name_with_prefix"));
    }

    /**
     * 测试非 get 开头的方法应该被忽略
     */
    @Test
    void testNonGetterMethodShouldBeIgnored() {
        TestBean bean = new TestBean();
        bean.setUserName("testUser");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testNonGetterMethodShouldBeIgnored", json);

        // fetchUserName 不是 getter，应该被忽略
        assertFalse(json.containsKey("fetch_user_name"));
        assertFalse(json.containsKey("user_name_fetched"));
    }

    /**
     * 测试 getClass 方法应该被忽略
     */
    @Test
    void testGetClassMethodShouldBeIgnored() {
        TestBean bean = new TestBean();

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testGetClassMethodShouldBeIgnored", json);

        // getClass 方法应该被忽略
        assertFalse(json.containsKey("class"));
    }

    /**
     * 测试空 Bean 对象
     */
    @Test
    void testEmptyBean() {
        TestBean bean = new TestBean();

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testEmptyBean", json);

        // 即使字段未设置，所有 getter 方法也会被包含在输出中
        assertNotNull(json);
        // TestBean 现在有 12 个 getter 方法：getUserName, getUserId, isActive, isClosed, isOpen, isA, isB, getIpAddress, getXmlHttpRequest, getHttpResponse, getX, getTags
        assertEquals(12, json.size());
        // 验证键名正确转换
        assertTrue(json.containsKey("user_name"));
        assertTrue(json.containsKey("user_id"));
        assertTrue(json.containsKey("active"));
        assertTrue(json.containsKey("closed"));
        assertTrue(json.containsKey("open"));
        assertTrue(json.containsKey("a"));
        assertTrue(json.containsKey("b"));
        assertTrue(json.containsKey("ip_address"));
        assertTrue(json.containsKey("xml_http_request"));
        assertTrue(json.containsKey("http_response"));
        assertTrue(json.containsKey("x"));
        assertTrue(json.containsKey("tags"));
    }

    /**
     * 测试混合场景
     */
    @Test
    void testMixedScenario() {
        TestBean bean = new TestBean();
        bean.setUserName("john");
        bean.setUserId(456);
        bean.setActive(true);
        bean.setClosed(false);
        bean.setOpen(true);
        bean.setIpAddress("10.0.0.1");
        bean.setXmlHttpRequest("<xml>");
        bean.setGet("skipMe");
        bean.setGetX("keepMe");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testMixedScenario", json);

        // TestBean 现在有 12 个 getter 方法
        assertEquals(12, json.size());
        assertEquals("john", json.getString("user_name"));
        assertEquals(456, json.getInteger("user_id"));
        assertTrue(json.getBoolean("active"));
        assertFalse(json.getBoolean("closed"));
        assertTrue(json.getBoolean("open"));
        assertEquals("10.0.0.1", json.getString("ip_address"));
        assertEquals("<xml>", json.getString("xml_http_request"));
        assertEquals("keepMe", json.getString("x"));
        // httpResponse 未设置，应为 null
        assertNull(json.getString("http_response"));
        // isA 和 isB 返回 false
        assertFalse(json.getBoolean("a"));
        assertFalse(json.getBoolean("b"));
        // tags 未设置，应为 null
        assertNull(json.getJsonArray("tags"));

        // 验证被忽略的字段
        assertFalse(json.containsKey("is_active"));
        assertFalse(json.containsKey(""));
        assertFalse(json.containsKey("get"));
        assertFalse(json.containsKey("class"));
    }

    /**
     * 测试单字母大写字段
     */
    @Test
    void testSingleLetterUppercaseField() {
        TestBean bean = new TestBean();
        bean.setGetX("singleX");

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testSingleLetterUppercaseField", json);

        assertEquals("singleX", json.getString("x"));
    }

    /**
     * 测试多个 boolean 字段使用 is 前缀的 getter 方法
     */
    @Test
    void testMultipleBooleanGettersWithIsPrefix() {
        TestBean bean = new TestBean();
        bean.setActive(true);
        bean.setClosed(false);
        bean.setOpen(true);

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testMultipleBooleanGettersWithIsPrefix", json);

        assertTrue(json.getBoolean("active"));
        assertFalse(json.getBoolean("closed"));
        assertTrue(json.getBoolean("open"));
    }

    /**
     * 测试 is 开头的方法是有效的 getter 方法（对应 boolean 字段）
     */
    @Test
    void testIsPrefixMethodsWithNonUppercaseThirdCharShouldBeIgnored() {
        TestBean bean = new TestBean();

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testIsPrefixMethodsWithNonUppercaseThirdCharShouldBeIgnored", json);

        // isA 和 isB 是有效的 getter 方法，应该被包含在输出中
        assertTrue(json.containsKey("a"));
        assertTrue(json.containsKey("b"));
        assertFalse(json.containsKey("is_a"));
        assertFalse(json.containsKey("is_b"));
    }

    /**
     * 测试 boolean 字段名称包含大写字母的正确转换
     */
    @Test
    void testBooleanFieldNameWithUppercaseLetters() {
        TestBean bean = new TestBean();
        bean.setClosed(true);
        bean.setOpen(false);

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testBooleanFieldNameWithUppercaseLetters", json);

        // isClosed 应该转换为 closed
        assertTrue(json.getBoolean("closed"));
        // isOpen 应该转换为 open
        assertFalse(json.getBoolean("open"));
    }

    /**
     * 测试 reloadData 方法 - 基本功能
     */
    @Test
    void testReloadDataBasic() {
        TestBean bean = new TestBean();
        JsonObject json = new JsonObject()
                .put("user_name", "Alice")
                .put("user_id", 789)
                .put("active", true)
                .put("ip_address", "192.168.2.1");

        printJsonOutput("testReloadDataBasic - Input JSON", json);

        bean.reloadData(json);

        printJsonOutput("testReloadDataBasic - After Reload", bean.toJsonObject());

        assertEquals("Alice", bean.getUserName());
        assertEquals(789, bean.getUserId());
        assertTrue(bean.isActive());
        assertEquals("192.168.2.1", bean.getIpAddress());
    }

    /**
     * 测试 reloadData 方法 - 下划线命名法转换为驼峰命名法
     */
    @Test
    void testReloadDataSnakeCaseToCamelCase() {
        TestBean bean = new TestBean();
        JsonObject json = new JsonObject()
                .put("user_name", "Bob")
                .put("ip_address", "10.0.0.5")
                .put("xml_http_request", "<test>");

        bean.reloadData(json);

        assertEquals("Bob", bean.getUserName());
        assertEquals("10.0.0.5", bean.getIpAddress());
        assertEquals("<test>", bean.getXmlHttpRequest());
    }

    /**
     * 测试 reloadData 方法 - 驼峰命名法（不包含下划线）
     */
    @Test
    void testReloadDataCamelCase() {
        TestBean bean = new TestBean();
        // 注意：如果 JSON 中直接使用驼峰命名法，可能无法匹配到 setter 方法
        // 因为当前的实现假设键是下划线命名法或已经正确转换为驼峰命名法
        JsonObject json = new JsonObject()
                .put("user_name", "Charlie");  // 使用下划线命名法

        bean.reloadData(json);

        assertEquals("Charlie", bean.getUserName());
    }

    /**
     * 测试 reloadData 方法 - boolean 类型字段
     */
    @Test
    void testReloadDataBooleanFields() {
        TestBean bean = new TestBean();
        JsonObject json = new JsonObject()
                .put("active", true)
                .put("closed", false)
                .put("open", true);

        bean.reloadData(json);

        assertTrue(bean.isActive());
        assertFalse(bean.isClosed());
        assertTrue(bean.isOpen());
    }

    /**
     * 测试 reloadData 方法 - 类型转换
     */
    @Test
    void testReloadDataTypeConversion() {
        TestBean bean = new TestBean();
        JsonObject json = new JsonObject()
                .put("user_name", "123")  // 字符串数字
                .put("user_id", 456);  // 整数

        bean.reloadData(json);

        assertEquals("123", bean.getUserName());  // 应该保持为字符串
        assertEquals(456, bean.getUserId());
    }

    /**
     * 测试 reloadData 方法 - 往返一致性
     */
    @Test
    void testReloadDataRoundTrip() {
        TestBean originalBean = new TestBean();
        originalBean.setUserName("Dave");
        originalBean.setUserId(999);
        originalBean.setActive(true);
        originalBean.setClosed(false);
        originalBean.setIpAddress("172.16.0.1");
        originalBean.setXmlHttpRequest("<original>");

        // 转换为 JSON
        JsonObject json = originalBean.toJsonObject();

        printJsonOutput("testReloadDataRoundTrip - Original Bean", json);

        // 创建新的 Bean 并从 JSON 重载数据
        TestBean newBean = new TestBean();
        newBean.reloadData(json);

        printJsonOutput("testReloadDataRoundTrip - Reloaded Bean", newBean.toJsonObject());

        // 验证数据一致性
        assertEquals(originalBean.getUserName(), newBean.getUserName());
        assertEquals(originalBean.getUserId(), newBean.getUserId());
        assertEquals(originalBean.isActive(), newBean.isActive());
        assertEquals(originalBean.isClosed(), newBean.isClosed());
        assertEquals(originalBean.getIpAddress(), newBean.getIpAddress());
        assertEquals(originalBean.getXmlHttpRequest(), newBean.getXmlHttpRequest());
    }

    /**
     * 测试 reloadData 方法 - 不存在的字段
     */
    @Test
    void testReloadDataNonExistentFields() {
        TestBean bean = new TestBean();
        bean.setUserName("Original");

        JsonObject json = new JsonObject()
                .put("user_name", "Updated")
                .put("non_existent_field", "value");  // 不存在的字段

        // 应该不会抛出异常
        assertDoesNotThrow(() -> bean.reloadData(json));

        assertEquals("Updated", bean.getUserName());
    }

    /**
     * 测试 reloadData 方法 - 空对象
     */
    @Test
    void testReloadDataEmptyJsonObject() {
        TestBean bean = new TestBean();
        bean.setUserName("Initial");

        JsonObject json = new JsonObject();

        // 应该不会抛出异常
        assertDoesNotThrow(() -> bean.reloadData(json));

        // 原有值应该保持不变
        assertEquals("Initial", bean.getUserName());
    }

    @Test
    void testNested1() {
        JsonObject jsonObject = new JsonObject()
                .put("a", new JsonObject()
                        .put("value", "123")
                );
        TestNestedBean testNestedBean = new TestNestedBean();
        testNestedBean.reloadData(jsonObject);
        printJsonOutput("testNested1", testNestedBean.toJsonObject());
        assertEquals("123", testNestedBean.getA().getValue());
    }

    /**
     * 测试 reloadData 方法 - JsonArray 类型
     */
    @Test
    void testReloadDataJsonArray() {
        TestBean bean = new TestBean();
        io.vertx.core.json.JsonArray tagsArray = new io.vertx.core.json.JsonArray()
                .add("java")
                .add("kotlin")
                .add("python");

        JsonObject json = new JsonObject()
                .put("user_name", "Developer")
                .put("tags", tagsArray);

        printJsonOutput("testReloadDataJsonArray - Input JSON", json);

        bean.reloadData(json);

        printJsonOutput("testReloadDataJsonArray - After Reload", bean.toJsonObject());

        assertEquals("Developer", bean.getUserName());
        assertNotNull(bean.getTags());
        assertEquals(3, bean.getTags().size());
        assertEquals("java", bean.getTags().get(0));
        assertEquals("kotlin", bean.getTags().get(1));
        assertEquals("python", bean.getTags().get(2));
    }

    /**
     * 测试 reloadData 方法 - JsonArray 为空数组
     */
    @Test
    void testReloadDataJsonArrayEmpty() {
        TestBean bean = new TestBean();
        io.vertx.core.json.JsonArray tagsArray = new io.vertx.core.json.JsonArray();

        JsonObject json = new JsonObject()
                .put("tags", tagsArray);

        bean.reloadData(json);

        assertNotNull(bean.getTags());
        assertTrue(bean.getTags().isEmpty());
    }

    /**
     * 测试 toJsonObject 方法 - JsonArray 类型
     */
    @Test
    void testToJsonObjectJsonArray() {
        TestBean bean = new TestBean();
        bean.setUserName("TestUser");
        bean.setTags(java.util.List.of("tag1", "tag2", "tag3"));

        JsonObject json = bean.toJsonObject();
        printJsonOutput("testToJsonObjectJsonArray", json);

        assertEquals("TestUser", json.getString("user_name"));
        assertTrue(json.containsKey("tags"));
        io.vertx.core.json.JsonArray tagsArray = json.getJsonArray("tags");
        assertEquals(3, tagsArray.size());
        assertEquals("tag1", tagsArray.getString(0));
        assertEquals("tag2", tagsArray.getString(1));
        assertEquals("tag3", tagsArray.getString(2));
    }

    /**
     * 测试 JsonArray 往返一致性
     */
    @Test
    void testJsonArrayRoundTrip() {
        TestBean originalBean = new TestBean();
        originalBean.setUserName("RoundTripUser");
        originalBean.setTags(java.util.List.of("a", "b", "c"));

        // 转换为 JSON
        JsonObject json = originalBean.toJsonObject();

        printJsonOutput("testJsonArrayRoundTrip - Original Bean", json);

        // 创建新的 Bean 并从 JSON 重载数据
        TestBean newBean = new TestBean();
        newBean.reloadData(json);

        printJsonOutput("testJsonArrayRoundTrip - Reloaded Bean", newBean.toJsonObject());

        // 验证数据一致性
        assertEquals(originalBean.getUserName(), newBean.getUserName());
        assertNotNull(newBean.getTags());
        assertEquals(originalBean.getTags(), newBean.getTags());
    }

    /**
     * 测试用的 Bean 类
     */
    static class TestBean implements JsonObjectMappedBean {
        private String userName;
        private int userId;
        private boolean active;  // 使用 is 前缀 getter
        private boolean closed;  // 使用 is 前缀 getter
        private boolean isOpen;  // 使用 is 前缀 getter
        private String ipAddress;
        private String xmlHttpRequest;
        private String httpResponse;
        private String get;  // 字段名为 get，对应方法 get()
        private String getX; // 字段名为 getX，对应方法 getX()
        private java.util.List<String> tags;  // JsonArray 类型示例

        public TestBean() {
        }

        @Override
        public String toJsonExpression() {
            return toJsonObject().encode();
        }

        @Override
        public String toFormattedJsonExpression() {
            return toJsonObject().encodePrettily();
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        // boolean 类型使用 is 前缀 getter
        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        // boolean 类型使用 is 前缀 getter
        public boolean isClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        // boolean 类型使用 is 前缀 getter
        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen(boolean open) {
            isOpen = open;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getXmlHttpRequest() {
            return xmlHttpRequest;
        }

        public void setXmlHttpRequest(String xmlHttpRequest) {
            this.xmlHttpRequest = xmlHttpRequest;
        }

        public String getHttpResponse() {
            return httpResponse;
        }

        public void setHttpResponse(String httpResponse) {
            this.httpResponse = httpResponse;
        }

        public String get() {
            return get;
        }

        public void setGet(String get) {
            this.get = get;
        }

        public String getX() {
            return getX;
        }

        public void setGetX(String getX) {
            this.getX = getX;
        }

        public java.util.List<String> getTags() {
            return tags;
        }

        public void setTags(java.util.List<String> tags) {
            this.tags = tags;
        }

        // 带参数的方法，应该被忽略
        public String getUserName(String prefix) {
            return prefix + userName;
        }

        // 非 get 开头的方法，应该被忽略
        public String fetchUserName() {
            return userName;
        }

        // is 开头但不是 getter（第三个字符不是大写），应该被忽略
        public boolean isA() {
            return false;
        }

        public boolean isB() {
            return false;
        }
    }

    static class TestNestedBean implements JsonObjectMappedBean {
        private TestNestedBeanA a;

        public TestNestedBeanA getA() {
            return a;
        }

        public TestNestedBean setA(TestNestedBeanA a) {
            this.a = a;
            return this;
        }
    }

    static class TestNestedBeanA implements JsonObjectMappedBean {
        private String value;

        public String getValue() {
            return value;
        }

        public TestNestedBeanA setValue(String value) {
            this.value = value;
            return this;
        }
    }
}