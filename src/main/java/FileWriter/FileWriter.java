package FileWriter;

import Model.Job;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileWriter {
    public static void writeDepartmentMapToJsonFile(Map<String, List<Job>> departmentMap, String fileName) {
        final Logger LOGGER = LoggerFactory.getLogger(FileWriter.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
            gson.toJson(departmentMap, writer);
            LOGGER.info("Department data written to {}", fileName);
        } catch (IOException e) {
            LOGGER.error("Error occurred during writing file: {}", fileName);
        }
    }
}
