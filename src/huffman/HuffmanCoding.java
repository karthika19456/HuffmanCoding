package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
        int[] numOcc = new int[128];
        int count = 0;
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            int curr = (int) c;
            numOcc[curr-1]++;
            count++;
        }
        sortedCharFreqList = new ArrayList<CharFreq>();
        for (int i = 0; i < numOcc.length; i++) {
            if (numOcc[i] > 0) {
                double prob = (double) numOcc[i]/count;
                Character c = (char)(i+1);
                sortedCharFreqList.add(new CharFreq(c, prob));
            }
        }
        if (sortedCharFreqList.size() == 1) {
            int i = (int) sortedCharFreqList.get(0).getCharacter();
            if (i == 127) {
                Character c = (char)(0);
                sortedCharFreqList.add(new CharFreq(c, 0));
            } else {
                Character c = (char)(i+1);
                sortedCharFreqList.add(new CharFreq(c, 0));
            }
        }
        Collections.sort(sortedCharFreqList);

    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<CharFreq> source = new Queue<CharFreq>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        // build source
        for (int i = 0; i < sortedCharFreqList.size(); i++) {
            source.enqueue(sortedCharFreqList.get(i));
        }

        // adding first 2 nodes

        huffmanRoot = new TreeNode();
        CharFreq p1 = source.dequeue();
        CharFreq p2 = source.dequeue();
        huffmanRoot.setData(new CharFreq(null, p1.getProbOcc() + p2.getProbOcc()));
        huffmanRoot.setLeft(new TreeNode(p1, null, null));
        huffmanRoot.setRight(new TreeNode(p2, null, null));
        target.enqueue(huffmanRoot);

        // finishing up the tree

        while (!source.isEmpty() || target.size() > 1) {
            TreeNode curr = new TreeNode();
            // look for left node
            if (!source.isEmpty() && (source.peek().getProbOcc() <= target.peek().getData().getProbOcc())) { // source is smaller or equal
                TreeNode l = new TreeNode(source.dequeue(), null, null);
                curr.setLeft(l);
            } else { // target is smaller
                TreeNode l = target.peek().getLeft();
                TreeNode r = target.peek().getRight();
                curr.setLeft(new TreeNode(target.dequeue().getData(), l, r));
            } 
            // look for right node
            if (target.size() < 1) { 
                TreeNode r = new TreeNode(source.dequeue(), null, null);
                curr.setRight(r);
            } else if ((!source.isEmpty()) && (source.peek().getProbOcc() <= target.peek().getData().getProbOcc())) { // source is smaller or equal
                TreeNode r = new TreeNode(source.dequeue(), null, null);
                curr.setRight(r);
            } else {
                TreeNode l = target.peek().getLeft();
                TreeNode r = target.peek().getRight();
                curr.setRight(new TreeNode(target.dequeue().getData(), l, r));
            }
            curr.setData(new CharFreq(null, curr.getLeft().getData().getProbOcc() + curr.getRight().getData().getProbOcc()));
            huffmanRoot = curr;
            target.enqueue(curr);
        }
    }

    private void createString(TreeNode x, String s) {
        if (x.getLeft() == null && x.getRight() == null) { // base case
            int index = (int) (x.getData().getCharacter());
            encodings[index] = s;
            return;
        }
        createString(x.getRight(), s.concat("1"));
        createString(x.getLeft(), s.concat("0"));
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];
        createString(huffmanRoot, "");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String s = "";
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            int x = (int) c;
            String t = encodings[x];
            s += t;
        }
        writeBitString(encodedFile, s);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        String file = readBitString(encodedFile);
        char[] c = file.toCharArray();

        TreeNode ptr = huffmanRoot;
        String s = "";

        for (int i = 0; i < c.length; i++) {
            if (ptr.getRight() == null || ptr.getLeft() == null) {
                char k = ptr.getData().getCharacter();
                s += k;
                ptr = huffmanRoot;
                continue;
            }
            if (c[i] == '0') {
                ptr = ptr.getLeft();
                if (ptr.getRight() == null || ptr.getLeft() == null) {
                    char k = ptr.getData().getCharacter();
                    s += k;
                    ptr = huffmanRoot;
                    continue;
                }
            } else {
                ptr = ptr.getRight();
                if (ptr.getRight() == null || ptr.getLeft() == null) {
                    char k = ptr.getData().getCharacter();
                    s += k;
                    ptr = huffmanRoot;
                    continue;
                }
            }
        }
        StdOut.print(s);
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
