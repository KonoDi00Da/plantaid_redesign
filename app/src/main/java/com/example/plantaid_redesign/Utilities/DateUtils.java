package com.example.plantaid_redesign.Utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DateUtils {

    public static LocalDate selectedDate;

    public DateUtils() {
    }

    public static String formatteFullMonth(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
        return date.format(formatter);
    }

    public static String monthYearFormatter(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
        return date.format(formatter);
    }

    public static String monthDayFormatter(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d");
        return date.format(formatter);
    }

    private static LocalDate sundayForDate(LocalDate current) {
        LocalDate oneWeekAgo = current.minusWeeks(1);

        while (current.isAfter(oneWeekAgo)){
            if (current.getDayOfWeek() == DayOfWeek.SUNDAY)
                return current;

            current = current.minusDays(1);
        }

        return null;
    }

    public static String convertDateString(String inputDate) {
        // Define the input and output date formats
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat outputDateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        try {
            // Parse the input date string to a Date object
            Date date = inputDateFormat.parse(inputDate);

            // Format the Date object to the desired output format
            String outputDate = outputDateFormat.format(date);
            return outputDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertTimeString(String inputTime) {
        // Define the input and output time formats
        DateFormat inputTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        DateFormat outputTimeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        try {
            // Parse the input time string to a Date object
            Date time = inputTimeFormat.parse(inputTime);

            // Format the Date object to the desired output format
            String outputTime = outputTimeFormat.format(time);
            return outputTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCurrentTime() {
        // Get calendar instance
        Calendar calendar = Calendar.getInstance();

        // Get current hour, minute, second, and millisecond
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);

        // Format time as "HH:mm:ss.SSS"
        String formattedTime = String.format("%02d:%02d:%02d.%03d", hour, minute, second, millisecond);

        return formattedTime;
    }

    public String getCurrentDate() {
        // Get calendar instance
        Calendar calendar = Calendar.getInstance();

        // Get current year, month, and day
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Note: Calendar.MONTH is zero-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Format date as "yyyy-MM-dd"
        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);

        return formattedDate;
    }
    public static String formattedTime(LocalTime time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return time.format(formatter);
    }

    public static String formatDate(LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int dayOfMonth = date.getDayOfMonth();
        return String.format("%s, %s %d", dayOfWeek, month, dayOfMonth);
    }

    public static String formattedDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    public static Calendar localDateToCalendar(LocalDate localDate){
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static String getMonth(String weekString) {
        // Parse the week number from the input string
        int week;
        if (weekString.contains("th")) {
            week = Integer.parseInt(weekString.split(" ")[0]);
        } else if (weekString.contains(" weeks")) {
            week = Integer.parseInt(weekString.split(" ")[0]);
        } else {
            return "Invalid week number";
        }

        // Determine the corresponding month based on the week number
        int month = (int) Math.ceil((double) week / 4);

        // Construct and return the month string
        switch (month) {
            case 1:
                return "First month";
            case 2:
                return "Second month";
            case 3:
                return "Third month";
            case 4:
                return "fourth month";
            case 5:
                return "fifth month";
            case 6:
                return "sixth month";
            case 7:
                return "seventh month";
            case 8:
                return "eight month";
            case 9:
            case 10:
                return "ninth month";
            default:
                return "Invalid week number";
        }
    }

    public static String formatDue(LocalDate dueDate){
        String formattedDate = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US).format(dueDate);
        return formattedDate;
    }

    public static String getCurrentWeek(LocalDate expectedDueDate, LocalDate currentDate) {
        long daysBetween = ChronoUnit.DAYS.between(currentDate, expectedDueDate);
        int weeksBetween = (int) Math.ceil(daysBetween / 7.0);
        int currentWeek = 40 - weeksBetween;

        String weekString = currentWeek + " weeks";

        return weekString;
    }

    public static String getCurrentDays(LocalDate expectedDueDate, LocalDate currentDate) {
        long daysBetween = ChronoUnit.DAYS.between(currentDate, expectedDueDate);
        int weeksBetween = (int) Math.ceil(daysBetween / 7.0);
        int currentWeek = 40 - weeksBetween;
        int remainingDays = (int) (daysBetween % 7);
        String weekString = currentWeek + " weeks";
        String dayString = remainingDays + " days";
        return dayString;
    }

    public static String getWeeks(LocalDate lmpDate, LocalDate currentDate){
        int days = (int) ChronoUnit.DAYS.between(lmpDate, currentDate);
        int weeks = days / 7;
        int remainingDays = days % 7;
        String weekString = weeks + " weeks";
        return weekString;
    }

    public static String getRemainingDays(LocalDate lmpDate, LocalDate currentDate){
        int days = (int) ChronoUnit.DAYS.between(lmpDate, currentDate);
        int weeks = days / 7;
        int remainingDays = days % 7;
        String remaining = remainingDays + " day/s";
        return remaining;
    }

    //this is not a Date Utils method. I just implement it here so that it will now take another line in where it is used.
    public static String generateRandomString() {
        int length = 8; // length of the random string
        String alphabet = "abcdefghijklmnopqrstuvwxyz"; // all the lowercase alphabets

        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            // get a random index from the alphabet string
            int index = random.nextInt(alphabet.length());

            // append the random character to the string builder
            sb.append(alphabet.charAt(index));
        }

        return sb.toString();
    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            String truncatedString = input.substring(0, maxLength);
            int lastSpaceIndex = truncatedString.lastIndexOf(" ");
            if (lastSpaceIndex != -1) {
                truncatedString = truncatedString.substring(0, lastSpaceIndex);
            }
            return truncatedString + "...";
        }
    }









}
