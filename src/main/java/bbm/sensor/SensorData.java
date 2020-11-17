/*
 * (C) Copyright 8 дек. 2018 г. Valentin Chirikov (http://ese.by/)
 * valc@ese.by +375 44 7629763
 */
package bbm.sensor;

import java.util.Objects;

/**
 * @author Valentin
 */
public class SensorData {

    private static final Float VOLTAGE_UNAVAILABLE = 65535f;
    private Float temp = 0f;
    private String id;
    private Float voltage = 0f;

    public Float getVoltage() {
        return voltage;
    }

    public void setVoltage(Float inVoltage) {
        if (inVoltage.equals(VOLTAGE_UNAVAILABLE)) {
            this.voltage = 0f;
        } else {
            this.voltage = inVoltage / 1000;//value from nodemcu come in 3033 format -  adc.readvdd33()
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.temp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SensorData other = (SensorData) obj;
        if (!Objects.equals(this.temp, other.temp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SensorData{" + "temp=" + temp + ", id=" + id + ", voltage=" + voltage + '}';
    }

}
