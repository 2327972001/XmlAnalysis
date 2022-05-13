package com.zym;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

/**
 * @author ZouYangMing
 */
public class Start {
    public static void main(String[] args) throws DocumentException, FileNotFoundException, UnsupportedEncodingException {
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        while (true){
            Scanner input_opt = new Scanner(System.in);
            System.out.println("==============================");
            System.out.println("1.自动翻译\n2.检测相同字符串\n3.退出");
            System.out.println("请输入需要使用的功能数字：");
            int opt = input_opt.nextInt();
            //自动翻译
            if(opt == 1){
                Scanner input_files = new Scanner(System.in);
                System.out.println("请输入需要解析的XML文件路径：");
                String file_path = input_files.nextLine();
                System.out.print("请输入保存的TXT文件路径或名称");
                System.err.println("（例如：a.txt 路径不能有中文）：");
                String save_path = input_files.nextLine();
                //2.加载xml
                File file = new File(file_path);
                Document document = reader.read(file);
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
                System.out.println("双击回车退出...");
                input_files.nextLine();
                break;
            }
            //检测相同字符串
            if(opt == 2){
                Scanner input_a_files = new Scanner(System.in);
                System.out.println("请输入需要检测的XML文件路径：");
                String a_file_path = input_a_files.nextLine();
                System.out.println("请输入提取出相同字符串保存的TXT文件路径或名称（例如：a.txt 路径不能有中文）：");
                String a_save_path = input_a_files.nextLine();
                System.out.println("请输入提取出没问题字符串保存的TXT文件路径或名称（例如：b.txt 路径不能有中文）：");
                String a_save2_path = input_a_files.nextLine();
                //加载xml
                File file = new File(a_file_path);
                Document a_document = reader.read(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                //获取根节点
                Element a_rootElement = a_document.getRootElement();
                //获取第二层节点
                Iterator a_iterator = a_rootElement.elementIterator();
                //存放id和text
                HashMap<String,String> a_map = new HashMap<>();
                while (a_iterator.hasNext()){
                    Element stu = (Element) a_iterator.next();
                    //获取第三层节点
                    Iterator a_iterator2 = stu.elementIterator();
                    while (a_iterator2.hasNext()){
                        Element stu2 = (Element) a_iterator2.next();
                        List<Attribute> a_attributes = stu2.attributes();
                        for (int i = 0; i < a_attributes.size(); i++) {
                            //过滤多余标签
                            if(a_attributes.get(i).getName() == "id" || a_attributes.get(i).getName() == "text"){
                                a_map.put(a_attributes.get(i).getValue(),a_attributes.get(i+1).getValue());
                                i = i+1;
                            }
                        }
                    }
                }
                HashMap<String, String> new_map = new HashMap<>();
                HashMap<String, String> new_map1 = new HashMap<>();
                a_map.forEach((k,v)->{
                    if (!new_map.containsValue(v)){
                        new_map.put(k,v);
                    }else{
                        new_map1.put(k,v);
                    }
                });
                //重复字符串
                String str = "";
                //没问题的字符串
                String str1 = "";
                for (Map.Entry<String, String> entry : new_map1.entrySet()) {
                    str += "<string id=\""+entry.getKey()+"\" text=\""+entry.getValue()+"\" />\n";;
                }
                for (Map.Entry<String, String> entry : new_map.entrySet()) {
                    str1 += "<string id=\""+entry.getKey()+"\" text=\""+entry.getValue()+"\" />\n";;
                }
                writeFile(str,a_save_path);
                writeFile(str1,a_save2_path);
                System.out.println("注意：提取出来的都是要修改的，因为已经保留了一个重复字符串没有提取");
                System.out.println("已把重复的提取出来了，请查看："+a_save_path);
                System.out.println("已把没问题的提取出来了，请查看："+a_save2_path);
                System.out.println("双击回车退出...");
                input_a_files.nextLine();
                break;
            }
            if(opt == 3){
                break;
            }
        }
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
