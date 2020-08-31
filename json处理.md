# Json的处理
## Java对多层嵌套json的简单处理
>依赖
```xml
<dependencies>
    <dependency>
        <groupId>net.sf.json-lib</groupId>
        <artifactId>json-lib</artifactId>
        <version>2.4</version>
        <classifier>jdk15</classifier>
    </dependency>
</dependencies>
```
>java处理嵌套json类似扁平映射
```java
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class pro_jsons {
    public static void main(String[] args) {
        String s = "{\n" +
                "    \"error\":0,\n" +
                "    \"status\":\"success\",\n" +
                "    \"results\":[\n" +
                "        {\n" +
                "            \"currentCity\":\"青岛\",\n" +
                "            \"index\":[\n" +
                "                {\n" +
                "                    \"title\":\"穿衣\",\n" +
                "                    \"zs\":\"较冷\",\n" +
                "                    \"tipt\":\"穿衣指数\",\n" +
                "                    \"des\":\"建议着厚外套加毛衣等服装。年老体弱者宜着大衣、呢外套加羊毛衫。\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"title\":\"紫外线强度\",\n" +
                "                    \"zs\":\"中等\",\n" +
                "                    \"tipt\":\"紫外线强度指数\",\n" +
                "                    \"des\":\"属中等强度紫外线辐射天气，外出时建议涂擦SPF高于15、PA+的防晒护肤品，戴帽子、太阳镜。\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        JSONObject jsonObject = JSONObject.fromObject(s);

        //提取出error为 0
        int error = jsonObject.getInt("error");
        System.out.println("error:" + error);

        //提取出status为 success
        String status = jsonObject.getString("status");
        System.out.println("status:" + status);

        //注意：results中的内容带有中括号[]，所以要转化为JSONArray类型的对象
        JSONArray result = jsonObject.getJSONArray("results");

        for (int i = 0; i < result.size(); i++) {
            //提取出currentCity为 青岛
            String currentCity = result.getJSONObject(i).getString("currentCity");
            System.out.println("currentCity:" + currentCity);

            //注意：index中的内容带有中括号[]，所以要转化为JSONArray类型的对象
            JSONArray index = result.getJSONObject(i).getJSONArray("index");

            for (int j = 0; j < index.size(); j++) {
                System.out.println("当前数字:"+j);
                String title = index.getJSONObject(j).getString("title");
                System.out.println("title:" + title);
                String zs = index.getJSONObject(j).getString("zs");
                System.out.println("zs:" + zs);
                String tipt = index.getJSONObject(j).getString("tipt");
                System.out.println("tipt:" + tipt);
                String des = index.getJSONObject(j).getString("des");
                System.out.println("des:" + des);

            }
        }
    }
}

```
## Python对多层嵌套json的简单处理
```python
import json
 
data = {
	"statusCode": 200,
	"data": {
		"totoal": "5",
		"height": "5.97",
		"weight": "10.30",
		"age": "11",
        "info":{"dick":"fuck"}
	},
	"msg": "成功"
}
 
#dumps:把字典转换为json字符串
s = json.dumps(data)
print(s)
 
#loads:把json转换为dict
s1 = json.loads(s)
print(s1)
print(s1["data"]["info"]["dick"])
```