package com.agrialert.alert_manager.domain;

import com.agrialert.R;
import com.agrialert.api.WeatherApiResponse;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.AlertWithThreshold;
import com.agrialert.data_manager.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        double temperature = weather.currentWeather.temperature;
        double windSpeed = weather.currentWeather.windspeed;
        double humidity = firstOrNaN(weather.hourly != null ? weather.hourly.relativeHumidity2m : null);
        double precipitation = firstOrNaN(weather.hourly != null ? weather.hourly.precipitation : null);

        for (AlertWithThreshold entry : activeAlerts) {
            if (entry == null) {
                continue;
            }

            AlertType type = entry.getAlertType();
            Double thresholdValue = entry.getThreshold();
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
                    if (!Double.isNaN(windSpeed) && windSpeed > threshold) {
                        result.add(build(type, "Vento forte",
                                "Vento > " + threshold + " km/h",
                                R.drawable.ic_alert_vento, field, groupName));
                    }
                    break;
                case "ONDATA_CALORE":
                    if (!Double.isNaN(temperature) && temperature > threshold) {
                        result.add(build(type, "Ondata di calore",
                                "Temperatura aria > " + threshold + " C",
                                R.drawable.ic_alert_calore, field, groupName));
                    }
                    break;
                case "GELO_BRINA":
                    if (!Double.isNaN(temperature) && temperature < threshold) {
                        result.add(build(type, "Gelo / brina",
                                "Temperatura minima < " + threshold + " C",
                                R.drawable.ic_alert_gelo, field, groupName));
                    }
                    break;
                case "SCARSA_VENTILAZIONE":
                    if (!Double.isNaN(windSpeed)
                            && !Double.isNaN(humidity)
                            && windSpeed < threshold
                            && humidity > HUMIDITY_MIN_SCARSA_VENTILAZIONE) {
                        result.add(build(type, "Scarsa ventilazione",
                                "Vento < " + threshold + " km/h e Umidita > " + HUMIDITY_MIN_SCARSA_VENTILAZIONE + "%",
                                R.drawable.ic_alert_ventilazione, field, groupName));
                    }
                    break;
                case "UMIDITA_ELEVATA":
                    if (!Double.isNaN(humidity) && humidity > threshold) {
                        result.add(build(type, "Umidita elevata",
                                "Umidita > " + threshold + "%",
                                R.drawable.ic_alert_umidita, field, groupName));
                    }
                    break;
                case "PIOGGIA_INTENSA":
                    if (!Double.isNaN(precipitation) && precipitation > threshold) {
                        result.add(build(type, "Pioggia intensa",
                                "Precipitazioni > " + threshold + " mm/h",
                                R.drawable.ic_alert_pioggia, field, groupName));
                    }
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private Alert build(AlertType type,
                        String title,
                        String description,
                        int iconRes,
                        Field field,
                        String groupName) {
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

    private double firstOrNaN(List<Double> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            return Double.NaN;
        }
        return list.get(0);
    }
}
