package org.edu.rabbitmq.beans;

/**
 * Created by
 * User: vkhodyre
 * Date: 1/18/2016
 */
public class Exchange {
    private String name;
    private String host;
    private String type;
    private String policy;

    public Exchange(String name, String host, String type, String policy) {
        this.name = name;
        this.host = host;
        this.type = type;
        this.policy = policy;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getType() {
        return type;
    }

    public String getPolicy() {
        return policy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Exchange exchange = (Exchange) o;

        if (!name.equals(exchange.name)) return false;
        if (!host.equals(exchange.host)) return false;
        if (!type.equals(exchange.type)) return false;
        return policy.equals(exchange.policy);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + policy.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", type='" + type + '\'' +
                ", policy='" + policy + '\'' +
                '}';
    }
}
