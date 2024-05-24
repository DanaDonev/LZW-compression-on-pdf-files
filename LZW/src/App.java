//Information technology project for compressing pdf file using LZW algorithm, and comparison of compression rates with other file formats
//Link to git repository with project presentation: https://github.com/DanaDonev/LZW-compression-on-pdf-files
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    // Constants for LZW compression
    private static final int BITS = 12; // Number of bits per code
    private static final int HASHING_SHIFT = 4; // Shift value for hashing
    private static final int MAX_VALUE = (1 << BITS) - 1; // Maximum code value
    private static final int MAX_CODE = MAX_VALUE - 1; // Maximum code (excluding EOF)
    private static final int TABLE_SIZE = 5021; // Size of the code table
    private static final int EOF = -1; // End of file marker

    // Buffered input and output streams
    private BufferedInputStream input = null;
    private BufferedOutputStream output = null;

    // Variables for managing output bit buffer
    private int output_bit_count = 0;
    private int output_bit_buffer = 0;

    // Code table arrays
    private short[] code_value = new short[TABLE_SIZE];
    private short[] prefix_code = new short[TABLE_SIZE];
    private short[] append_character = new short[TABLE_SIZE];

    // Constructor to initialize input and output streams
    public App(FileInputStream input, FileOutputStream output) {
        this.input = new BufferedInputStream(input);
        this.output = new BufferedOutputStream(output);
    }

    // Method to find matching code in the code table
    private int findMatch(int hash_prefix, int hash_character) {
        int index = (hash_character << HASHING_SHIFT) ^ hash_prefix;
        int offset = (index == 0) ? 1 : TABLE_SIZE - index;
        while (true) {
            if (code_value[index] == EOF) {
                return index;
            }
            if (prefix_code[index] == hash_prefix && append_character[index] == hash_character) {
                return index;
            }
            index -= offset;
            if (index < 0) {
                index += TABLE_SIZE;
            }
        }
    }

    // Method to compress the input data using LZW algorithm
    public void compress() throws IOException {
        int next_code = 256;  // Next code value to be used
        for (int i = 0; i < TABLE_SIZE; i++) {
            code_value[i] = EOF; // Initialize code values
        }

        int current_code = input.read(); // Read the first byte
        if (current_code == EOF) {
            return; // Empty input
        }

        int input_byte;
        while ((input_byte = input.read()) != EOF) {
            int index = findMatch(current_code, input_byte);
            if (code_value[index] != EOF) {
                current_code = code_value[index];
            } else {
                outputCode(current_code); // Output the current code
                if (next_code <= MAX_CODE) {
                    code_value[index] = (short) next_code++; // Add new code to table
                    prefix_code[index] = (short) current_code;
                    append_character[index] = (short) input_byte;
                }
                current_code = input_byte;
            }
        }
        outputCode(current_code); // Output the last code
        outputCode(MAX_VALUE); // Output end of file marker
        flushBuffer(); // Flush remaining bits in buffer
    }

    // Method to write a code to the output stream
    private void outputCode(int code) throws IOException {
        output_bit_buffer |= (code << (32 - BITS - output_bit_count));
        output_bit_count += BITS;
        while (output_bit_count >= 8) {
            output.write(output_bit_buffer >> 24); // Write the highest byte to output
            output_bit_buffer <<= 8;
            output_bit_count -= 8;
        }
    }

    // Method to flush remaining bits in the output buffer
    private void flushBuffer() throws IOException {
        while (output_bit_count > 0) {
            output.write(output_bit_buffer >> 24); // Write the highest byte to output
            output_bit_buffer <<= 8;
            output_bit_count -= 8;
        }
        output.flush(); // Flush the output stream
    }

    // Method to read a PDF file into a byte array
    public static byte[] readPDFIntoByteArray(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // Method to get the size of a file
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }

    // Main method
    public static void main(String[] args) {
        String pdfFilePath = "..\\files\\CNknjiga.pdf"; // Path to input PDF file
        String outputFilePath = "..\\files\\CNknjiga_compressed.lzw"; // Path to output compressed file

        try {
            // Step 1: Read the PDF file into a byte array
            byte[] bytes = readPDFIntoByteArray(pdfFilePath);

            // Step2: Write the byte array to a temporary file for compression
            String tempFilePath = "temp_pdf_bytes.bin";
            Files.write(Paths.get(tempFilePath), bytes);

            // Step 3: Compress the binary data using LZW compression
            try (FileInputStream inputFileStream = new FileInputStream(tempFilePath);
                 FileOutputStream outputFileStream = new FileOutputStream(outputFilePath)) {

                App lzw = new App(inputFileStream, outputFileStream);
                lzw.compress();
                System.out.println("Compression completed successfully.");
            }

            // Step 4: Obtain the sizes of the PDF file and other document formats
long downloadedPdfFileSize = getFileSize("..\\files\\CNknjigaDownloaded.pdf"); // Downloaded PDF file size
long pdfFileSize = getFileSize(pdfFilePath); // PDF file size
long compressedFileSize = getFileSize(outputFilePath); // LZW compressed file size
long djvuFileSize = getFileSize("..\\files\\CNknjiga.djvu"); // DJVU file size
long zipFileSize = getFileSize("..\\files\\CNknjiga.zip"); // ZIP compressed file size
long mobiFileSize = getFileSize("..\\files\\CNknjiga.mobi"); // MOBI file size
long txtFileSize = getFileSize("..\\files\\CNknjiga.txt"); // TXT file size
//the next two formats are larger in size and therefore could not be uploaded to git
long htmlFileSize = 33195  //getFileSize("..\\files\\CNknjiga.html"); // HTML file size
long rtfFileSize = 43598  //getFileSize("..\\files\\CNknjiga.rtf"); // RTF file size

// Step 5: Compare the sizes and calculate compression ratios
double downloadedPdfCompressionRatio = (double) downloadedPdfFileSize / pdfFileSize;
double pdfCompressionRatio = (double) compressedFileSize / pdfFileSize;
double djvuCompressionRatio = (double) djvuFileSize / pdfFileSize;
double zipCompressionRatio = (double) zipFileSize / pdfFileSize;
double mobiCompressionRatio = (double) mobiFileSize / pdfFileSize;
double txtCompressionRatio = (double) txtFileSize / pdfFileSize;
double htmlCompressionRatio = (double) htmlFileSize / pdfFileSize;
double rtfCompressionRatio = (double) rtfFileSize / pdfFileSize;

// Step 6: Print the results
System.out.println("Compression ratio (PDF to Downloaded PDF): " + downloadedPdfCompressionRatio);
System.out.println("Compression ratio (PDF to Lempel-Ziv): " + pdfCompressionRatio);
System.out.println("Compression ratio (PDF to DJVu): " + djvuCompressionRatio);
System.out.println("Compression ratio (PDF to ZIP): " + zipCompressionRatio);
System.out.println("Compression ratio (PDF to MOBI): " + mobiCompressionRatio);
System.out.println("Compression ratio (PDF to TXT): " + txtCompressionRatio);
System.out.println("Compression ratio (PDF to HTML): " + htmlCompressionRatio);
System.out.println("Compression ratio (PDF to RTF): " + rtfCompressionRatio);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
