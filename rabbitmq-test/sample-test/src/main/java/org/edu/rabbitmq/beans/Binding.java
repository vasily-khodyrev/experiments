package org.edu.rabbitmq.beans;

/**
 * Created by
 * User: vkhodyre
 * Date: 1/18/2016
 */
public class Binding {
    private String source;
    private String host;
    private String destination;
    private String destinationType;
    private String routingKey;

    public Binding(String source, String host, String destination, String destinationType, String routingKey) {
        this.source = source;
        this.host = host;
        this.destination = destination;
        this.destinationType = destinationType;
        this.routingKey = routingKey;
    }

    public String getSource() {
        return source;
    }

    public String getHost() {
        return host;
    }

    public String getDestination() {
        return destination;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binding binding = (Binding) o;

        if (!source.equals(binding.source)) return false;
        if (!host.equals(binding.host)) return false;
        if (!destination.equals(binding.destination)) return false;
        if (!destinationType.equals(binding.destinationType)) return false;
        return routingKey.equals(binding.routingKey);

    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + destinationType.hashCode();
        result = 31 * result + routingKey.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", destination='" + destination + '\'' +
                ", destinationType='" + destinationType + '\'' +
                ", routingKey='" + routingKey + '\'' +
                '}';
    }
}
