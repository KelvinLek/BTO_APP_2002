package repository;

import entity.Application;
import pub_enums.ApplStatus;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationRepo {
    private final Map<String, Application> applicationsMap = new HashMap<>();
    private static final String APPLICATION_FILE = "ApplicationList.csv";

    public ApplicationRepo() {
        loadFromCsv();
    }

    private void loadFromCsv() {

    }
}