package top.aprilyolies.curator.extension;

/**
 * @Author EvaJohnson
 * @Date 2019-06-06
 * @Email g863821569@gmail.com
 */
public class InstanceDetails {
    private String description;

    public InstanceDetails() {
        this("");
    }

    public InstanceDetails(String description) {
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}