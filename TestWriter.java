import java.io.*;
public class TestWriter {
    String filepath;
    File file;
	public void TestWriter(int count, String service, String queue){
                        
                        filepath = "C:/Users/huahan/Documents/NJIT/CS673/QueueSimulator/test.txt";
                        file = new File("filepath");
		try {
			 
			String content = "";
 
			for(int i = 0; i < count; i++){
				content = content + (int)(Math.random() * 10 + 1) + "|" + service + "|" + queue + "\n";
			}
			

 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
                        
                        System.out.println("Done");
 
			
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        public void main(){
            TestWriter(300,"Custom", "SuperFast");
        }
}
