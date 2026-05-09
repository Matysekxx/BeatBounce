package cz.matysekxx.beatbounce.model.skin;

public class SkinData {
    private String id;
    private String name;
    private int price;
    private String primaryColor;
    private String glowColor;

    public record SkinVisuals(String renderType, boolean trailEnabled) {}
    private SkinVisuals visuals;

    public SkinData(String id, String name, int price, String primaryColor, String glowColor, SkinVisuals visuals) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.primaryColor = primaryColor;
        this.glowColor = glowColor;
        this.visuals = visuals;
    }

    public SkinData() {}

    public String getId() { return id; }

    public String getName() { return name; }

    public int getPrice() { return price; }

    public String getPrimaryColor() { return primaryColor; }

    public String getGlowColor() { return glowColor; }

    public SkinVisuals getVisuals() { return visuals; }
}
