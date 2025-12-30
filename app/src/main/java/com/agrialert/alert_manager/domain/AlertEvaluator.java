package com.agrialert.alert_manager.domain;

import com.agrialert.R;
import com.agrialert.api.WeatherApiResponse;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.AlertWithThreshold;
import com.agrialert.data_manager.Field;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Converte i dati meteo in potenziali alert applicando semplici soglie.
 */
public class AlertEvaluator {

    private static final int DEFAULT_ICON_RES = R.drawable.ic_alert;
    private static final int HUMIDITY_MIN_SCARSA_VENTILAZIONE = 80;

    public List<Alert> evaluate(WeatherApiResponse weather,
                                Field field,
                                String groupName,
                                List<AlertWithThreshold> activeAlerts) {
        List<Alert> result = new ArrayList<>();

        if (weather == null || weather.currentWeather == null || field == null) {
            return result;
        }
        if (activeAlerts == null || activeAlerts.isEmpty()) {
            return result;
        }

        // Serie orarie (possibili fino a 7 giorni)
        List<String> timeSeries = weather.hourly != null ? weather.hourly.time : null;
        List<Double> tempSeries = weather.hourly != null ? weather.hourly.temperature2m : null;
        List<Double> humiditySeries = weather.hourly != null ? weather.hourly.relativeHumidity2m : null;
        List<Double> precipitationSeries = weather.hourly != null ? weather.hourly.precipitation : null;
        List<Double> windSeries = weather.hourly != null ? weather.hourly.windSpeed10m : null;

        for (AlertWithThreshold entry : activeAlerts) {
            if (entry == null) {
                continue;
            }

            AlertType type = entry.getAlertType();
            //Double thresholdValue = entry.getThreshold();
            Double thresholdValue = 31d;
            if (type == null || thresholdValue == null) {
                continue;
            }
            double threshold = thresholdValue;
            String key = normalizeTypeName(type.getName());
            if (key == null) {
                continue;
            }

            switch (key) {
                case "VENTO_FORTE":
                    addIfForecastMatches(result, type, field, groupName, windSeries, timeSeries,
                            value -> value > threshold,
                            "Vento forte",
                            value -> "Previsto vento " + formatValue(value, "km/h") + " (soglia " + threshold + ")",
                            R.drawable.ic_alert_vento);
                    break;
                case "ONDATA_CALORE":
                    addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries,
                            value -> value > threshold,
                            "Ondata di calore",
                            value -> "Prevista T aria " + formatValue(value, "C") + " (soglia " + threshold + ")",
                            R.drawable.ic_alert_calore);
                    break;
                case "GELO_BRINA":
                    addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries,
                            value -> value < threshold,
                            "Gelo / brina",
                            value -> "Prevista T aria " + formatValue(value, "C") + " (soglia " + threshold + ")",
                            R.drawable.ic_alert_gelo);
                    break;
                case "SCARSA_VENTILAZIONE":
                    addIfForecastMatchesPaired(result, type, field, groupName, windSeries, humiditySeries, timeSeries,
                            value -> value < threshold,
                            hum -> hum > HUMIDITY_MIN_SCARSA_VENTILAZIONE,
                            "Scarsa ventilazione",
                            (windVal, humVal) -> "Previsti vento " + formatValue(windVal, "km/h") +
                                    " e umidita " + formatValue(humVal, "%") +
                                    " (soglia vento " + threshold + ", umidita > " + HUMIDITY_MIN_SCARSA_VENTILAZIONE + "%)",
                            R.drawable.ic_alert_ventilazione);
                    break;
                case "UMIDITA_ELEVATA":
                    addIfForecastMatches(result, type, field, groupName, humiditySeries, timeSeries,
                            value -> value > threshold,
                            "Umidita elevata",
                            value -> "Prevista umidita " + formatValue(value, "%") + " (soglia " + threshold + ")",
                            R.drawable.ic_alert_umidita);
                    break;
                case "PIOGGIA_INTENSA":
                    addIfForecastMatches(result, type, field, groupName, precipitationSeries, timeSeries,
                            value -> value > threshold,
                            "Pioggia intensa",
                            value -> "Previste precipitazioni " + formatValue(value, "mm/h") + " (soglia " + threshold + ")",
                            R.drawable.ic_alert_pioggia);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private void addIfForecastMatches(List<Alert> result,
                                      AlertType type,
                                      Field field,
                                      String groupName,
                                      List<Double> values,
                                      List<String> times,
                                      ThresholdPredicate predicate,
                                      String title,
                                      ValueFormatter descriptionFormatter,
                                      int iconRes) {
        int idx = firstIndexMatching(values, predicate);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double value = safeGet(values, idx);
        String description = descriptionFormatter.format(value) + formatWhen(forecastAt);
        result.add(build(type, title, description, iconRes, field, groupName, forecastAt));
    }

    private void addIfForecastMatchesPaired(List<Alert> result,
                                            AlertType type,
                                            Field field,
                                            String groupName,
                                            List<Double> seriesA,
                                            List<Double> seriesB,
                                            List<String> times,
                                            ThresholdPredicate predicateA,
                                            ThresholdPredicate predicateB,
                                            String title,
                                            BiValueFormatter descriptionFormatter,
                                            int iconRes) {
        int idx = firstIndexMatching(seriesA, predicateA, seriesB, predicateB);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double valA = safeGet(seriesA, idx);
        double valB = safeGet(seriesB, idx);
        String description = descriptionFormatter.format(valA, valB) + formatWhen(forecastAt);
        result.add(build(type, title, description, iconRes, field, groupName, forecastAt));
    }

    private int firstIndexMatching(List<Double> values, ThresholdPredicate predicate) {
        if (values == null || predicate == null) return -1;
        for (int i = 0; i < values.size(); i++) {
            double v = safeGet(values, i);
            if (!Double.isNaN(v) && predicate.test(v)) {
                return i;
            }
        }
        return -1;
    }

    private int firstIndexMatching(List<Double> seriesA,
                                   ThresholdPredicate predicateA,
                                   List<Double> seriesB,
                                   ThresholdPredicate predicateB) {
        if (seriesA == null || seriesB == null || predicateA == null || predicateB == null) return -1;
        int size = Math.min(seriesA.size(), seriesB.size());
        for (int i = 0; i < size; i++) {
            double a = safeGet(seriesA, i);
            double b = safeGet(seriesB, i);
            if (!Double.isNaN(a) && !Double.isNaN(b) && predicateA.test(a) && predicateB.test(b)) {
                return i;
            }
        }
        return -1;
    }

    private Alert build(AlertType type,
                        String title,
                        String description,
                        int iconRes,
                        Field field,
                        String groupName,
                        long forecastAt) {
        Alert alert = new Alert();
        alert.setFieldId(field.getId());
        alert.setGroupName(groupName != null ? groupName : field.getGroupName());
        alert.setTypeId(type.getId());
        String resolvedTitle = title != null ? title : type.getName();
        alert.setTitle(resolvedTitle);
        String resolvedDescription = description;
        if (resolvedDescription == null || resolvedDescription.isEmpty()) {
            resolvedDescription = type.getDescription();
        }
        alert.setDescription(resolvedDescription);
        alert.setFieldAddress(field.getAddress());
        alert.setForecastAt(forecastAt);
        alert.setCreatedAt(System.currentTimeMillis());
        alert.setResolved(false);
        alert.setIconRes(iconRes != 0 ? iconRes : DEFAULT_ICON_RES);
        return alert;
    }

    private String normalizeTypeName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private double safeGet(List<Double> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Double.NaN;
        }
        Double value = list.get(index);
        return value != null ? value : Double.NaN;
    }

    private long parseIsoTime(List<String> times, int index) {
        if (times == null || index < 0 || index >= times.size()) {
            return 0L;
        }
        try {
            // Open-Meteo usa ISO8601, es. 2025-01-01T12:00
            String iso = times.get(index);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = df.parse(iso);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private String formatWhen(long timestampMs) {
        if (timestampMs <= 0L) return "";
        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        return " - previsto per " + df.format(new Date(timestampMs));
    }

    private String formatValue(double value, String suffix) {
        if (Double.isNaN(value)) return "-";
        return String.format(Locale.getDefault(), "%.1f %s", value, suffix);
    }

    private interface ThresholdPredicate {
        boolean test(double value);
    }

    private interface ValueFormatter {
        String format(double value);
    }

    private interface BiValueFormatter {
        String format(double valueA, double valueB);
    }
}
