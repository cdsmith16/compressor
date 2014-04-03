import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;


public class Compressor {
	String window;
	SpecByte topByte; //special byte pointing to linked list of output bytes
	private final int MAX_DISTANCE;
	private final int MAX_LENGTH;
	String inputFile;
	String outputFile;
	BufferedReader reader;
	
	private class SpecByte{
		public byte cur;
		public SpecByte next;
		public int filled;
		
		public SpecByte(byte cur, int filled){
			this.cur = cur;
			this.filled = filled;
			this.next = null;

		}
		
		public void appendZero(char ch){
			System.out.println(""+0+","+ch);
			SpecByte curByte = this;
			while(curByte.next!= null){
				curByte = curByte.next;
			}
			byte curMask = (byte) ch;
			curMask = (byte) (curMask >>> (curByte.filled+1));
			curByte.cur = (byte) (curByte.cur | curMask);
			byte nextByte = (byte) (ch << (8-(curByte.filled+1)));
			nextByte = (byte) (nextByte & 0xFF);
			if(curByte.filled == 7){
				curByte.next = new SpecByte(nextByte, 8);
				curByte.next.next = new SpecByte((byte)0,0);
			} else {
				curByte.next = new SpecByte(nextByte, curByte.filled+1);
			}
			
			return;
		}
		
		public void appendOne(int distance, int length){
			System.out.println(""+1+","+distance+","+length);
			SpecByte curByte = this;
			while(curByte.next!= null){
				curByte = curByte.next;
			}
			int origFilled = curByte.filled;
			byte curMask;
			
			if(curByte.filled == 0){
				curByte.cur = (byte) (curByte.cur | (byte) 128);
			} else {
				byte addOnes = (byte) 64;
				addOnes = (byte) (addOnes >>> (curByte.filled-1));
				curByte.cur = (byte) (curByte.cur | addOnes);
			}
			
			int distLen = distance;
			distLen = (distLen << 4);
			distLen = (distLen | length);
			
			byte lenMask = (byte) distLen;
			distLen = (distLen >>> 8);
			byte distMask = (byte) distLen;
			
			curMask = distMask;
			curMask = (byte) (curMask >> (origFilled +1));
			curByte.cur = (byte) (curByte.cur | curMask);
			
			byte nextByte = distMask;
			nextByte = (byte) (nextByte << (8-(origFilled +1)));
			curMask = lenMask;
			curMask = (byte) (curMask >> (origFilled +1));
			nextByte = (byte) (nextByte | curMask);
			curByte.next = new SpecByte(nextByte, 8);
			curByte = curByte.next;
			
			byte lastByte = lenMask;
			lastByte = (byte) (lastByte << (8-(origFilled +1)));
			
			if(origFilled == 7){
				curByte.next = new SpecByte(lastByte, 8);
				curByte.next.next = new SpecByte((byte)0,0);
			} else {
				curByte.next = new SpecByte(lastByte, origFilled+1);
			}
	
			return;
		}
		
	}

	public Compressor(String inputFile, String outputFile) throws FileNotFoundException {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		MAX_DISTANCE = (1 << 11); //12 bit pointers
		MAX_LENGTH = (1 << 3); //4 bit lengths
	}
	
	private void printAllBytes(SpecByte sb){
		String str = String.format("%8s", Integer.toBinaryString(sb.cur & 0xFF)).replace(" ", "0");
		System.out.println(str);
		while(sb.next!=null){
			sb = sb.next;
			str = String.format("%8s", Integer.toBinaryString(sb.cur & 0xFF)).replace(" ", "0");
			System.out.println(str);
			System.out.println((char)sb.cur);
		}
	}
	
	public void Compress() throws IOException{
		reader = new BufferedReader(new FileReader(inputFile));
		topByte = new SpecByte((byte) 0,0);
		String window = "";
		
		String curWord = "";
		int a = 0;
		
		while((a = reader.read())>-1){
			curWord += (char) a;
			int last = window.lastIndexOf(curWord);
			if(last>-1){
				int distance = window.length()-last;
				reader.mark(1);
				while(((a = reader.read())>-1)&&curWord.length()<MAX_LENGTH){
					curWord += (char) a;
					last = window.lastIndexOf(curWord);
					if(last==-1){
						curWord = curWord.substring(0,curWord.length()-1);
						reader.reset();
						break;
					} else {
						distance = window.length()-last;
					}
					
					reader.mark(1);
				}
				topByte.appendOne(distance, curWord.length());
				window += curWord;
				curWord = "";
			} else {
				topByte.appendZero((char) a);
				window += (char) a;
				curWord = "";
			}
			
			if(window.length()>=MAX_DISTANCE){
				int diff = window.length()-MAX_DISTANCE;
				window = window.substring(diff);
			}

		}
		
		reader.close();
		printAllBytes(topByte);
		return;
	}

	
	public void WriteOutputBinary(String outputFile){
		FileOutputStream out = null;

		try {
			SpecByte sb = topByte;
		    out =  new FileOutputStream(outputFile);
		    out.write(sb.cur);
		    while(sb.next!=null){
		    	sb = sb.next;
		    	out.write(sb.cur);
		    }
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
		Compressor compressor = new Compressor("/Users/Mom/Documents/AndroidStuff/compressor/src/test.txt",null);
		compressor.Compress();
		compressor.WriteOutputBinary("/Users/Mom/Documents/AndroidStuff/compressor/src/testOutput.txt");
		
		
		/**
		String str = String.format("%8s", Integer.toBinaryString(127)).replace(" ", "$");
		System.out.println(str);
		str = String.format("%16s", Integer.toBinaryString(325)).replace(" ", "$");
		System.out.println(str);
		System.out.println(Integer.numberOfLeadingZeros(25));
		*/
	}

}
