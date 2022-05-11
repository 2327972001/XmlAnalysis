package com.zym;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * @author ZouYangMing
 */
public class Start {
    public static void main(String[] args) throws DocumentException {
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        //2.加载xml
        Scanner input_files = new Scanner(System.in);
        System.out.println("请输入需要解析的XML文件路径：");
        String file_path = input_files.nextLine();
        System.out.print("请输入保存的TXT文件路径");
        System.err.println("（注意：需要先创建一个txt文件）：");
        String save_path = input_files.nextLine();
        Document document = reader.read(new File(file_path));
        //3.获取根节点
        Element rootElement = document.getRootElement();
        Iterator iterator = rootElement.elementIterator();
        String str = "";
        System.err.println("开始运行...请耐心等待...");
        while (iterator.hasNext()){
            Element stu = (Element) iterator.next();
            List<Attribute> attributes = stu.attributes();
            for (Attribute attribute : attributes) {
                if(attribute.getName() == "name"){
                    String[] a = attribute.getValue().split("}");
                    if(a.length == 2){
                        RestTemplate restTemplate = new RestTemplate();
                        String url = "https://api.tjit.net/api/fanyi/?key=BsmDcnNxJi24M8xKFwumRVw9gL&text="+a[1]+"&from=en&to=zh";
                        JSONObject result = restTemplate.getForObject(url, JSONObject.class);
                        String text = result.getString("data");
                        text = text.substring(text.indexOf("dst=")+4,text.indexOf("}]}"));
                        str += "<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />\n";
                        //中文字符串校验
                        if(!text.matches("[\\u4e00-\\u9fa5]+")){
                            System.out.println("翻译失败，请手动翻译："+"<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />");
                        }else{
                            System.out.println("翻译成功："+"<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />");
                        }
                    }
                }
            }
        }
        writeFile(str,save_path);
        System.err.println("自动翻译成功，请查看："+save_path);
        System.out.println("按回车退出...");
        input_files.nextLine();
    }

    /**
     * 把字符串写入文件
     * @param str
     */
    public static void writeFile(String str,String file_path) {
        File file = new File(file_path);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(str);
            fileWriter.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
