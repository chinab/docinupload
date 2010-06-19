import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注册机
 * @author Jeff
 * @date 2010-6-6 下午02:26:28
 */
public class GetEncoding {

	/**
	 * 替换的字符
	 */
	private static String keys[]=new String[]{"bigbiGworLd","taobAo.gaOqs.Com"};
	
	/**
	 * 
	 * @author Jeff
	 * @date 2010-6-6 下午02:20:09
	 * @param args
	 */
	public static void main(String a[]){
		while(true){
		Console console = System.console();
			if (console == null) {
				System.out.println("不能使用控制台");
			}
			String macSecurity = console.readLine("请输入注册码：\n");
		        
			if(macSecurity!=null && macSecurity.length()>0){
				System.out.println(encodingSecurityMac(macSecurity));
			}else{
				System.out.println(encodingSecurityMac(getSecurityMac()));
			}
		}
	}

	/**
	 * 验证注册码
	 * @author Jeff
	 * @date 2010-5-29 下午11:05:43
	 * @param input
	 * @return
	 */
	public static String encodingSecurityMac(String input){
		String temp=Md5Util.md5(input, keys[1]);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<temp.length();i+=2){
			sb.append(temp.charAt(i));
		}
		return sb.toString(); 
	}
	
	/**
	 * 得到mac地址
	 * @author Jeff
	 * @date 2010-5-24 下午04:54:26
	 * @return
	 */
	public static String getSecurityMac(){
		String line;
		String physicalAddress = "read MAC error!";
		try {
			Process p = Runtime.getRuntime().exec("cmd.exe /c ipconfig /all");
			// p.waitFor();
			BufferedReader bd = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			while ((line = bd.readLine()) != null) {
				//用正则取mac地址
				//中间为:-或空格的mac地址
				Matcher mc = Pattern.compile("([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})").matcher(line);
				if(mc.find()){
					physicalAddress= mc.group();
				}else{
					//中间无任何符号的mac
					mc = Pattern.compile("([0-9a-fA-F]{2})(([0-9a-fA-F]{2}){5})").matcher(line);
					if(mc.find()){
						physicalAddress= mc.group();
					}
				}
				
			}
			p.waitFor();
		} catch (Exception e) {
			System.out.println("无法生成注册码：");
			return "";
		}
		if(physicalAddress.equals("read MAC error!")){
			System.out.println("无法生成注册码：");
			return "";
		}
		return Md5Util.md5(physicalAddress, keys[0]);
	}
}
