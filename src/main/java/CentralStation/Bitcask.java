package CentralStation;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Bitcask {

    private final String directoryPath;
    private Map<String, Map.Entry<String, Long>> indexMap;
    private long currentFileNumber = 0;
    private File currentFile;
    private RandomAccessFile currentFileStream;

    public Bitcask(String directoryPath) {
        this.directoryPath = directoryPath;
        File directory = new File(directoryPath);
        directory.mkdirs();
        this.indexMap = new HashMap<>();
        this.currentFileNumber = 0;
        if (directory.listFiles().length > 0 && recover()) {
            currentFileNumber++;
        }
        initialize();
    }

    private void initialize() {
        currentFile = new File(directoryPath + "/" + currentFileNumber + ".bitcask");
        try {
            currentFile.createNewFile();
            currentFileStream = new RandomAccessFile(currentFile, "rw");}
        catch (IOException e) {
             e.printStackTrace();}
    }

    private boolean recover() {
        List<String> bitcaskFileNames = getSortedFileNames(directoryPath, ".bitcask");
        if (bitcaskFileNames.isEmpty())
            return false;
        List<String> hintFileNames = getSortedFileNames(directoryPath, ".hint");

        try {
            String lastHintFile = hintFileNames.get(hintFileNames.size() - 1);
            this.indexMap = loadHintFiles(lastHintFile);

            if (bitcaskFileNames.size() == hintFileNames.size()) {
                this.currentFileNumber = extractFileNumber(lastHintFile);
                return true;}
            else {
                String latestBitcaskFile = bitcaskFileNames.get(bitcaskFileNames.size() - 1);
                File file = new File(latestBitcaskFile);
                RandomAccessFile fileStream = new RandomAccessFile(file, "r");

                while (fileStream.getFilePointer() < fileStream.length()) {
                    long offset = fileStream.getFilePointer();
                    int keyLength = fileStream.readInt();
                    byte[] keyBytes = new byte[keyLength];
                    fileStream.readFully(keyBytes);

                    int valueLength = fileStream.readInt();
                    byte[] valueBytes = new byte[valueLength];
                    fileStream.readFully(valueBytes);

                    indexMap.put(new String(keyBytes, StandardCharsets.UTF_8),
                            new AbstractMap.SimpleEntry<>(file.getCanonicalPath(), offset));
                }
                fileStream.close();
                this.currentFileNumber = extractFileNumber(latestBitcaskFile);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public List<String> getSortedFileNames(String directoryPath, String suffix) {
        List<String> previousFileNames = Arrays.stream(Objects.requireNonNull(new File(directoryPath).listFiles()))
                .filter(f -> f.getName().endsWith(suffix))
                .sorted(Comparator.comparing(File::getName))
                .map(File::getName)
                .collect(Collectors.toList());
        return previousFileNames;
    }

    public int extractFileNumber(String fileName) {
        int endIndex = fileName.indexOf('_');
        String fileNumberStr;
        if (endIndex != -1) {
            fileNumberStr = fileName.substring(0, endIndex);
        } else {
            fileNumberStr = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return Integer.parseInt(fileNumberStr);
    }

    public void put(String key, String value) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(8 + keyBytes.length + valueBytes.length);

            buffer.putInt(keyBytes.length);
            buffer.put(keyBytes);
            buffer.putInt(valueBytes.length);
            buffer.put(valueBytes);

            long offset = currentFileStream.length();
            currentFileStream.seek(offset);
            currentFileStream.write(buffer.array());
            indexMap.put(key, new AbstractMap.SimpleEntry<>(currentFile.getCanonicalPath(), offset));
            if (currentFileStream.length() > 10_000) {
                rotateFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();}
    }


    public String get(String key) {
        if (!indexMap.containsKey(key))
            return null;

        try {
            Map.Entry<String, Long> value = indexMap.get(key);
            String filePath = value.getKey();
            Long offset = value.getValue();

            if (currentFile.getCanonicalPath().equals(filePath)) {
                currentFileStream.seek(offset);
                int keyLength = currentFileStream.readInt();
                byte[] keyBytes = new byte[keyLength];
                currentFileStream.readFully(keyBytes);

                int valueLength = currentFileStream.readInt();
                byte[] valueBytes = new byte[valueLength];
                currentFileStream.readFully(valueBytes);

                return new String(valueBytes, StandardCharsets.UTF_8);
            } else {
                RandomAccessFile tempFileStream = new RandomAccessFile(new File(filePath), "r");
                tempFileStream.seek(offset);
                int keyLength = tempFileStream.readInt();
                byte[] keyBytes = new byte[keyLength];
                tempFileStream.readFully(keyBytes);

                int valueLength = tempFileStream.readInt();
                byte[] valueBytes = new byte[valueLength];
                tempFileStream.readFully(valueBytes);
                tempFileStream.close();
                return new String(valueBytes, StandardCharsets.UTF_8);
            }}catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void rotateFile() {
        try {
            currentFileStream.close();
            createHintFiles(this.indexMap, currentFile.getName());
            if (currentFileNumber % 3 == 0) {
                compact();
            } else {
                currentFileNumber++;
                currentFile = new File(directoryPath + "/" + currentFileNumber + ".bitcask");
                currentFile.createNewFile();
                currentFileStream = new RandomAccessFile(currentFile, "rw");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compact() {
        try {
            this.currentFileNumber++;
            long newFileNumber = currentFileNumber;
            File newFile = new File(directoryPath + "/" + newFileNumber + "_compact.bitcask");
            newFile.createNewFile();
            RandomAccessFile newFileStream = new RandomAccessFile(newFile, "rw");
            Map<String, Map.Entry<String, Long>> newIndexMap = new HashMap<>();

            for (Map.Entry<String, Map.Entry<String, Long>> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                String filePath = entry.getValue().getKey();
                Long offset = entry.getValue().getValue();
                RandomAccessFile fileStream = new RandomAccessFile(new File(filePath), "r");
                fileStream.seek(offset);

                int keyLength = fileStream.readInt();
                byte[] keyBytes = new byte[keyLength];
                fileStream.readFully(keyBytes);

                int valueLength = fileStream.readInt();
                byte[] valueBytes = new byte[valueLength];
                fileStream.readFully(valueBytes);
                fileStream.close();

                byte[] keyBytesNew = keyBytes;
                byte[] valueBytesNew = valueBytes;
                long offsetNew = newFileStream.length();
                ByteBuffer buffer = ByteBuffer.allocate(8 + keyBytesNew.length + valueBytesNew.length);
                buffer.putInt(keyBytesNew.length);
                buffer.put(keyBytesNew);
                buffer.putInt(valueBytesNew.length);
                buffer.put(valueBytesNew);

                newFileStream.seek(offsetNew);
                newFileStream.write(buffer.array());

                newIndexMap.put(key, new AbstractMap.SimpleEntry<>(newFile.getCanonicalPath(), offsetNew));
            }

            currentFileStream.close();
            currentFile = newFile;
            currentFileStream = newFileStream;
            indexMap.clear();
            indexMap.putAll(newIndexMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createHintFiles(Map<String, Map.Entry<String, Long>> indexMap, String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(directoryPath + "/" + fileName + ".hint");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(indexMap);
            System.out.println("Serialized data is saved in " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map.Entry<String, Long>> loadHintFiles(String fileName) {
        Map<String, Map.Entry<String, Long>> newIndexMap = null;
        try (FileInputStream fileIn = new FileInputStream(directoryPath + "/" + fileName);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            newIndexMap = (Map<String, Map.Entry<String, Long>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return newIndexMap;
    }
}