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
        Scanner input_files = new Scanner(System.in);
        System.out.println("请输入需要解析的XML文件路径：");
        String file_path = input_files.nextLine();
        System.out.print("请输入保存的TXT文件路径或名称");
        System.err.println("（例如：a.txt 路径不能有中文）：");
        String save_path = input_files.nextLine();
        //2.加载xml
        Document document = reader.read(new File(file_path));
        //3.获取根节点
        Element rootElement = document.getRootElement();
        //获取第二层节点
        Iterator iterator = rootElement.elementIterator();
        String str = "";
        System.err.println("开始运行...请耐心等待...");
        while (iterator.hasNext()){
            Element stu = (Element) iterator.next();
            List<Attribute> attributes = stu.attributes();
            //获取第三层节点
            Iterator iterator2 = stu.elementIterator();
            while (iterator2.hasNext()){
                Element stu2 = (Element) iterator2.next();
                //获取第四层节点
                Iterator iterator3 = stu2.elementIterator();
                while (iterator3.hasNext()){
                    Element stu3 = (Element) iterator3.next();
                    List<Attribute> attributes3 = stu3.attributes();
                    for (Attribute attribute : attributes3) {
                        //过滤多余标签
                        if(attribute.getName() == "name" &&
                                        (stu2.getName()!="child_character_templates" &&
                                                stu2.getName()!="notable_and_wanderer_templates" &&
                                                stu2.getName()!="lord_templates" &&
                                                stu2.getName()!="rebellion_hero_templates" &&
                                                stu2.getName()!="tournament_team_templates_one_participant" &&
                                                stu2.getName()!="tournament_team_templates_two_participant" &&
                                                stu2.getName()!="tournament_team_templates_four_participant")){
                            String[] a = attribute.getValue().split("}");
                            str += zh(attribute);
                        }
                    }
                }
            }
            for (Attribute attribute : attributes) {
                str += zh(attribute);
            }
        }
        writeFile(str,save_path);
        System.err.println("自动翻译成功，请查看："+save_path);
        System.out.println("按回车退出...");
        input_files.nextLine();
    }

    /**
     * 翻译
     * @param attribute
     */
    public static String zh(Attribute attribute) {
        String str = "";
        if(attribute.getName() == "name"){
            String[] a = attribute.getValue().split("}");
            //判断是否拿到value值
            if(a.length == 2){
                RestTemplate restTemplate = new RestTemplate();
                String url = "https://api.tjit.net/api/fanyi/?key=BsmDcnNxJi24M8xKFwumRVw9gL&text="+a[1]+"&from=auto&to=zh";
                JSONObject result = restTemplate.getForObject(url, JSONObject.class);
                String text = result.getString("data");
                if(text.indexOf("error_msg") == -1){
                    text = text.substring(text.indexOf("dst=")+4,text.indexOf("}]}"));
                    str = "<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />\n";
                    //中文字符串校验
                    if(!text.matches("[\\u4e00-\\u9fa5]+")){
                        System.out.println("翻译失败，请手动翻译："+"<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />");
                    }else{
                        System.out.println("翻译成功："+"<string id=\""+a[0].replace("{=","")+"\" text=\""+text+"\" />");
                    }
                }else{
                    System.out.println("连接超时，正在重试...");
                    zh(attribute);
                }
            }
        }
        return str;
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
