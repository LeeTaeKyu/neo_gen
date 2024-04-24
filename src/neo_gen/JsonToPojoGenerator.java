package neo_gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <pre>
 *   neo_gen
 *      |_ JsonToPojoGenerator.java
 * </pre>
 *
 * @desc    :
 * @author  : jy.lee
 * @date    : 2024.04.15
 * ===========================================================
 * DATE             AUTHOR          NOTE
 * -----------------------------------------------------------
 * 2024.04.15       jy.lee          최초작성
 */
public class JsonToPojoGenerator {

    public final static String ROOT_NODE= "root";

    /**
     * TODO 아래 변수들은 사용자 입력으로 받을 예정
     *==================================================================
     */
    public static String INPUT_DIR_ROOT = "D:/json2pojo/json";  // input folder path
    public static String OUPUT_DIR_ROOT = "D:/json2pojo/java";  // output folder path
    public static boolean IS_INNERCLASS = false;                // InnderClass 생성여부 (true: json파일당 하나의 InnerClass로 생성/ false: 각각 java로 생성)
    public static String MODEL_TYPE     = "DTO";                // pojo모델패턴 (VO, DTO, DAO)
    public static String PAKAGE_PATH    = "";                   // java pakage 경로
    /**==================================================================*/

    public static SourceModelDTO smDTO;
    public static ArrayList<SourceModelDTO> smDTOList;

    public static int currDepth         = 0;
    public static String currParentKey  = "";
    public static String sendParentKey  = "";
    public static String tempNodeString = "";

    public static void jsonToPojo(String filePath, String outputFolder, String packagePath, String modelType, boolean isInnerClass)  {

        if(outputFolder != null && !outputFolder.isBlank()) {
            OUPUT_DIR_ROOT = outputFolder;
        }

        if(packagePath != null && !packagePath.isBlank()) {
            PAKAGE_PATH = packagePath;
        }

        if(modelType != null && !modelType.isBlank()) {
            MODEL_TYPE = modelType;
        }
        IS_INNERCLASS = isInnerClass;

        File file = new File(filePath);

        Map<String, Object> map = new HashMap<String, Object>();
        String newFileName   = "";
        String className     = "";
        String fileExt       = "";

        try {
            newFileName = file.getName();

            System.out.println(">>>>>> [" + newFileName + "] 파일 변환시작");
            System.out.println("...................................................................................");

            fileExt = newFileName.substring(newFileName.lastIndexOf(".") + 1);
            newFileName = newFileName.substring(newFileName.lastIndexOf("\\") + 1);
            newFileName = newFileName.substring(0, newFileName.lastIndexOf("."));

            System.out.println("step-1 >>>>> JSON 파일 읽기 [시작] .................");
            map = stringToMap(getTemplateFile(file));
            System.out.println("step-1 <<<<<< JSON 파일 읽기 [종료] ................");

            smDTOList = new ArrayList<SourceModelDTO>();

            // 재귀로 노드 판별
            convertRecurAddList(map);
            System.out.println("step-2 >>>>> JSON -> Map으로 변환 [시작] ...........");
            className = toCamelCase(newFileName, "class") + MODEL_TYPE;
            System.out.println("step-2 >>>>> JSON -> Map으로 변환 [종료] ...........");

            if(IS_INNERCLASS) {
                // InnerClass 패턴
                createInnerClassJavaSource(smDTOList, className);
            } else {
                // 단일 Class 패턴
                createMonoClassJavaSource(smDTOList, className);
            }
            System.out.println("3. 자바소스 변환 [종료] .................................................................");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    /**
     * @desc	: Convert JSON String to Map
     * @method	: stringToMap
     * @param json
     * @return
     */
    public static Map<String, Object> stringToMap(String json) {

        ObjectMapper mapper     = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(json, Map.class);
        } catch (IOException e) {
            System.out.println("[ERROR] JSON 포멧에러!!");
        }
        return map;
    }

    /**
     * @desc	: MAP(tree구조) => LIST에 담기 (초기호출용)
     * @method	: convertRecurAddList
     * @param map
     */
    public static void convertRecurAddList(Map<String, Object> map) {
        convertRecurAddList(map, 0, ROOT_NODE);
    }

    /**
     * @desc	: MAP(tree구조) => LIST에 담기
     * @method	: convertRecurAddList
     * @param map
     * @param depth
     * @param parentKey
     */
    private static void convertRecurAddList(Map<String, Object> map, int depth, String parentKey) {

        String strFieldType = "";

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String key      = entry.getKey();
            Object value    = entry.getValue();
            boolean isParent= (value != null) && ("LinkedHashMap".equals(getObjectType(value)) || "ArrayList".equals(getObjectType(value)));

            if(depth == 0) {
                currParentKey  = ROOT_NODE;
                tempNodeString = ROOT_NODE;
            }

            Map<String, Object> childMap = null;
            boolean isSubNode = false;

            // 부모노드면..
            if(isParent) {

                if(currDepth < depth) {
                    tempNodeString += "_"+key;
                    currParentKey = tempNodeString.split("_")[depth];
                 } else {
                     String temp = "";
                     for(int i = 0 ; i <= depth; i++ ) {
                         temp += (i > 0 ? "_" : "") +tempNodeString.split("_")[i];
                     }
                     tempNodeString = temp + "_"+key;
                     currParentKey = tempNodeString.split("_")[depth];
                 }

                sendParentKey = key;

                strFieldType = "";
                if (getObjectType(value).contains("ArrayList")) {

                    strFieldType = "ArrayList";
                    ArrayList<Object> arrayList = (ArrayList<Object>) value;

                    if(arrayList.size() > 0) {
                        if(getObjectType(arrayList.get(0)).contains("LinkedHashMap")) {
                            childMap     = (Map<String, Object>) arrayList.get(0);
                            strFieldType = "ArrayList<" + changeInitCap(sendParentKey) + MODEL_TYPE + ">";
                        } else {
                            strFieldType = "ArrayList<" + changeInitCap(getObjectType(arrayList.get(0))) + ">";
                        }
                    }
                } else {
                    strFieldType = changeInitCap(sendParentKey) + MODEL_TYPE;
                    childMap     = (Map<String, Object>) value;
                }

                // 하위노드 존재시
                if(childMap != null) {
                    isSubNode = true;
                }
            } else {
                strFieldType  = getObjectType(value);
                currParentKey = parentKey;
            }

            //smDTO = new SourceModelDTO(depth, currParentKey, strFieldType, key);
            smDTO = new SourceModelDTO();
            smDTO.setDepth(depth);
            smDTO.setClassName(currParentKey);
            smDTO.setFieldType(strFieldType);
            smDTO.setFieldName(key);

            smDTOList.add(smDTO);
//          System.out.println(smDTOList);

            currDepth = depth;

            // 하위노드가 존재한다면 재귀호출
            if (isSubNode) {
                convertRecurAddList(childMap, depth + 1, sendParentKey);
            }
        }
    }

    /**
     * @desc    : 자바소스파일 생성(InnerClass형식)
     * @method  : createInnerClassJavaSource
     * @param smDTOList
     * @param className
     * @param mainClassName
     * @return
     */
    public static void createInnerClassJavaSource(ArrayList<SourceModelDTO> smDTOList, String mainClassName) {

        StringBuffer sbf      = new StringBuffer();
        StringBuffer rbf      = new StringBuffer();
        String className      = "";
        String strJavaSource  = "";
        String checkDupField  = "";

        // className으로 List그룹핑
        Map<String, List<SourceModelDTO>> groupSourceModelMap = smDTOList.stream().collect(Collectors.groupingBy(SourceModelDTO::getClassName));

        sbf.append(getIntroJavaSource(mainClassName, className));
        for (Map.Entry<String, List<SourceModelDTO>> entry : groupSourceModelMap.entrySet()) {

            Object value = entry.getValue();
            List<SourceModelDTO> list = (List<SourceModelDTO>) value;

            className  = toCamelCase(entry.getKey(), "class") + MODEL_TYPE;

            if(!ROOT_NODE.equals(entry.getKey())) {
                sbf.append("\n");
                sbf.append("    @Getter\n");
                sbf.append("    @Setter\n");
                sbf.append("    public class ");
                sbf.append(className + " { \n\n");
            }

            for(SourceModelDTO sourceModelDTO : list) {

                if(checkDupField.contains("|"+sourceModelDTO.getFieldName()+"|")) {
                    continue;
                }
                checkDupField += "|"+sourceModelDTO.getFieldName()+"|";

                if(!ROOT_NODE.equals(entry.getKey())) {
                    sbf.append("        private " + sourceModelDTO.getFieldType() + " " + sourceModelDTO.getFieldName() + ";\n");
                } else {
                    rbf.append("    private " + sourceModelDTO.getFieldType() + " " + sourceModelDTO.getFieldName() + ";\n");
                }
            }

            if(!ROOT_NODE.equals(entry.getKey())) {
                sbf.append("    } \n");
            }
            checkDupField = "";
        }
        sbf.append("} \n");

        strJavaSource = sbf.toString();
        strJavaSource = strJavaSource.replaceAll("#####", rbf.toString());

        System.out.println(strJavaSource);
        createFile(strJavaSource, mainClassName, "");

        System.out.println("...................................................................................");
        System.out.println(">>>>>> [" + mainClassName + ".java" + "] 파일 변환완료 \n\n");
    }

    /**
     * @desc    : 자바소스파일 생성(단일 Class형식)
     * @method  : createMonoClassJavaSource
     * @param smDTOList
     * @param className
     * @param mainClassName
     * @return
     */
    public static void createMonoClassJavaSource(ArrayList<SourceModelDTO> smDTOList, String mainClassName) {

        StringBuffer sbf;
        String className      = "";
        String strJavaSource  = "";
        String checkDupField  = "";

        // className으로 List그룹핑
        Map<String, List<SourceModelDTO>> groupSourceModelMap = smDTOList.stream().collect(Collectors.groupingBy(SourceModelDTO::getClassName));

        for (Map.Entry<String, List<SourceModelDTO>> entry : groupSourceModelMap.entrySet()) {

            Object value = entry.getValue();
            List<SourceModelDTO> list = (List<SourceModelDTO>) value;

            className  = entry.getKey();
            className  = ROOT_NODE.equals(className) ? mainClassName : toCamelCase(className, "class") + MODEL_TYPE;

            sbf = new StringBuffer();
            sbf.append(getIntroJavaSource(mainClassName, className));

            for(SourceModelDTO sourceModelDTO : list) {

                if(checkDupField.contains("|"+sourceModelDTO.getFieldName()+"|")) {
                    continue;
                }
                checkDupField += "|"+sourceModelDTO.getFieldName()+"|";

                sbf.append("    private " + sourceModelDTO.getFieldType() + " " + sourceModelDTO.getFieldName() + ";");
                sbf.append("\n");
            }
            sbf.append("\n}");

            checkDupField = "";
            strJavaSource = sbf.toString();

            System.out.println(strJavaSource);
            System.out.println("...................................................................................");
            System.out.println(">>>>>> [" + className + ".java" + "] 파일 변환완료 \n\n");
            createFile(strJavaSource, className, mainClassName);
        }
    }

    /**
     * @desc	: Java 상단선언부 소스취득
     * @method	: getIntroJavaSource
     * @param mainClassName
     * @param className
     * @return
     */
    public static String getIntroJavaSource(String mainClassName,  String className) {

        String packagePath  = "";
        StringBuffer sbf    = new StringBuffer();

        if(IS_INNERCLASS) {
            packagePath = PAKAGE_PATH;
            className   = mainClassName;
        } else {
            packagePath = PAKAGE_PATH + (!"".equals(PAKAGE_PATH) ? "." : "")  + mainClassName;
        }

        sbf.append("package "+ packagePath +"; \n\n");
        sbf.append("import lombok.Data; \n");
        sbf.append("import lombok.EqualsAndHashCode; \n\n");
        sbf.append("/**\n");
        sbf.append(" * <pre>\n");
        sbf.append(" * "+ packagePath +"\n");
        sbf.append(" *      |_ " + className +".java \n");
        sbf.append(" * </pre>\n");
        sbf.append(" *\n");
        sbf.append(" * @desc : [JSON -> "+ MODEL_TYPE +"] Generated Source \n");
        sbf.append(" */\n");
        sbf.append("@Getter\n");
        sbf.append("@Setter\n");
        sbf.append("@EqualsAndHashCode(callSuper = false)\n");
        sbf.append("public class ");
        sbf.append(className + " { \n\n");

        if(IS_INNERCLASS) {
            sbf.append("#####");
        }
        return sbf.toString();
    }

    /**
     * @desc    : 파일내용 String에 담기
     * @method  : getTemplateFile
     * @param file
     * @return
     * @throws IOException
     */
    public static String getTemplateFile(File file) throws IOException {

        String contents = "";

        // try (..) => 이 안에서 파일을 열어주면 마지막에 finally 없어도 됨~
        try (
            FileReader fr       = new FileReader(file);
            BufferedReader br   = new BufferedReader(fr);
        ) {
            String readLine = "";
            while ((readLine = br.readLine()) != null) {
                contents += readLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    /**
     * @desc    : JavaObject 타입 취득
     * @method  : getObjectType
     * @param obj
     * @return
     */
    public static String getObjectType(Object obj) {

        String className    = obj.getClass().getName();
        String objectType   = "String";
        String stringValue  = "";

        if(className.contains("String")) {

            stringValue = obj.toString().replaceAll("^<|>$", "").toLowerCase();

            if("integer".equals(stringValue)) {
                objectType = "Integer";
            } else if("double".equals(stringValue)) {
                objectType = "Double";
            } else if("boolean".equals(stringValue)) {
                objectType = "Boolean";
            } else if("datetime".equals(stringValue) || "date-time".equals(stringValue)) {
                objectType = "Timestamp";
            }
        } else if(className.contains("Integer")) {
            objectType = "Integer";
        } else if(className.contains("Double")) {
            objectType = "Double";
        } else if(className.contains("Boolean")) {
            objectType = "Boolean";
        } else if(className.contains("LinkedHashMap")) {
            objectType = "LinkedHashMap";
        } else if(className.contains("ArrayList")) {
            objectType = "ArrayList";
        }
        return objectType;
    }

    /**
     * @desc    : 부모노드 문자열 취득
     * @method  : parentNodeString
     * @param tempNodeString
     * @param depth
     * @return
     */
    public static String parentNodeString(String tempNodeString, int depth) {

        String rtnString = "";

        if(depth == 0) {
            rtnString = ROOT_NODE + "_";
        } else {
            for( int i = 0 ; i <= depth ; i++ ) {
                rtnString += tempNodeString.split("_")[i] +"_";
            }
        }
        return rtnString;
    }

    /**
     * @desc    : 자바파일 생성
     * @method  : createFile
     * @param generatorVo
     * @param tableName
     */
    public static void createFile(String strJavaSource, String fileName, String addFolder) {

        String filename   = fileName + ".java";
        String saveFolder = OUPUT_DIR_ROOT;

        if(!"".equals(addFolder)) {
            saveFolder += "/" + addFolder;
        }

        File folder = new File(saveFolder);

        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(saveFolder, filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            bw.write(strJavaSource);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * @desc    : 카멜표기로 변환
     * @method  : toCamelCase
     * @param s
     * @param div
     * @return
     */
    public static String toCamelCase(String s, String div) {

        String rtnValue = "";
        String camel    = "";

        if (s.isEmpty()) {
            return "";
        }

        if(s.contains("_")) {
            s = s.toLowerCase();
            camel = String.join("",  Arrays.stream(s.split("[-_]")).map(item -> item.substring(0, 1).toUpperCase() + item.substring(1)).toArray(String[]::new));
        } else {
            camel = s;
        }

        rtnValue = s.substring(0, 1) + camel.substring(1);

        if ("class".equals(div)) {
            rtnValue = changeInitCap(rtnValue);
        }
        return rtnValue;
    }

    /**
     * 첫 글자를 대문자로 치환
     * @param str
     * @return
     */
    public static String changeInitCap(String str) {

        char[] arrChar = str.toCharArray();
        arrChar[0] = Character.toUpperCase(arrChar[0]);

        return String.valueOf(arrChar);
    }

    /**
     * <pre>SourceModelDTO</pre>
     * @desc : 자바소스 구성요소 DTO
     */
    static class SourceModelDTO {

        public int depth;
        public String className;
        public String fieldType;
        public String fieldName;

        public int getDepth() {
            return depth;
        }
        public void setDepth(int depth) {
            this.depth = depth;
        }
        public String getClassName() {
            return className;
        }
        public void setClassName(String className) {
            this.className = className;
        }
        public String getFieldType() {
            return fieldType;
        }
        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
        public String getFieldName() {
            return fieldName;
        }
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String toString() {
            return ">>"+this.depth+","+this.className+","+this.fieldType+","+this.fieldName+"\n";
        }
    }

}