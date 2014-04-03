import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class DeCompressor {
	BufferedReader reader;
	String inputFile;
	String outputFile;

	public DeCompressor(String inputFile, String outputfile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	private char GetFromZero(int cur, int next, int index){
		byte base = (byte) (cur << (index+1));
		byte mask = (byte) (next >> (8-(index+1)));
		base = (byte) (base | mask);
		char ch = (char) base;
		System.out.println(ch);
		return ch;
	}
	
	private String GetFromOne(int cur, int first, int second, String file, int index){
		byte len = (byte) (cur << (index+1));
		len = (byte) (len >>> (index+1)); //truncates extra info from cur
		int length = len;
		length = (length << 8); //moves to beginning of 16bit short
		length = (length | first);
		length = (length << (index+1));
		second = (second >> (8-(index+1)));
		length = (length | second);
		int distance = length; //get the values first, now do work
		
		short trueLength = (short) length;
		trueLength = (short) (trueLength << 12);
		trueLength = (short) (trueLength >>> 12);
		
		distance = (distance >>> 4);
		
		int start = (file.length()-distance);
		
		String sub = file.substring(start, start + trueLength);
		System.out.println(sub);
		
		return sub;
	}
	
	public void DeCompress() throws IOException{
		reader = new BufferedReader(new FileReader(inputFile));
		String file = "";
		
		int a = 0;
		int index = -1;
		while((a=reader.read())>-1){
			byte b = (byte) a;
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
				reader.reset();
			} else {
				//it's a 0
				reader.mark(1);
				int next = reader.read();
				char ch = GetFromZero(b,next,index);
				file += ch;
				reader.reset();
			}
		}
		System.out.println(file);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DeCompressor dc = new DeCompressor("/Users/Mom/Documents/AndroidStuff/compressor/src/testOutput.txt",null);
		dc.DeCompress();

	}

}
