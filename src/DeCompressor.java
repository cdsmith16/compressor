import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class DeCompressor {
	BufferedInputStream reader;
	String inputFile;
	String outputFile;
	String output = "";

	public DeCompressor(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	private char GetFromZero(int cur, int next, int index){
		byte base = (byte) (cur << (index+1));
		byte mask = (byte) (next >>> (8-(index+1)));
		base = (byte) (base | mask);
		char ch = (char) base;
		//System.out.println(ch);
		return ch;
	}
	
	private String GetFromOne(int cur, int first, int second, String file, int index){
		int len = (byte) (cur << (index+1));
		len = (byte) (len >>> (index+1)); //truncates extra info from cur
		int length = len;
		length = (length << 8); //moves to beginning of 16bit short
		length = (length | first);
		length = (length << (index+1));
		second = (second >>> (8-(index+1)));
		length = (length | second);
		int dist = length; //get the values first, now do work
		
		length = (length & 15);
		
		dist = (dist >>> 4);
		short distance = (short) dist;
		
		if(length < 0){
			System.out.print("coo");
		}
		
		int start = (file.length()-distance);
		
		String sub = file.substring(start, start + length);
		//System.out.println(sub);
		
		return sub;
	}
	
	public void DeCompress() throws IOException{
		reader = new BufferedInputStream(new FileInputStream(inputFile));
		String file = "";
		
		int a = 0;
		int index = -1;
		while((a=reader.read())>-1){
			int b = a;
			index++;
			index = index%8;
			int compare = (int) (Math.pow(2, (7-index)));
			compare = (~compare);
			compare = (compare | b);
			if((byte)compare==(byte)0xFF){
				//it's a 1
				int first = reader.read();
				reader.mark(1);
				int second = reader.read();
				String sub = GetFromOne(b,first,second,file,index);
				file += sub;
				if(index!=7)reader.reset();
			} else {
				//it's a 0
				reader.mark(1);
				int next = reader.read();
				char ch = GetFromZero(b,next,index);
				file += ch;
				if(index!=7)reader.reset();
			}
		}
		output = (file);
	}
	
	public void WriteOutputBinary(){
		FileWriter out = null;

		try {
		    out =  new FileWriter(outputFile);
		    out.write(output);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {out.close();} catch (Exception ex) {}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DeCompressor dc = new DeCompressor(args[0],args[1]);
		dc.DeCompress();
		dc.WriteOutputBinary();

	}

}
