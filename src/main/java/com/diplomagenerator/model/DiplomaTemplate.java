package com.diplomagenerator.model;

public class DiplomaTemplate {
    private String templatePath;
    private Orientation orientation;
    private float namePositionX;
    private float namePositionY;
    private float fontSize;

    public DiplomaTemplate(String templatePath, Orientation orientation, 
                          float namePositionX, float namePositionY, float fontSize) {
        this.templatePath = templatePath;
        this.orientation = orientation;
        this.namePositionX = namePositionX;
        this.namePositionY = namePositionY;
        this.fontSize = fontSize;
    }

    // Геттеры
    public String getTemplatePath() { return templatePath; }
    public Orientation getOrientation() { return orientation; }
    public float getNamePositionX() { return namePositionX; }
    public float getNamePositionY() { return namePositionY; }
    public float getFontSize() { return fontSize; }
    
    // Сеттеры
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
    public void setOrientation(Orientation orientation) { this.orientation = orientation; }
    public void setNamePositionX(float namePositionX) { this.namePositionX = namePositionX; }
    public void setNamePositionY(float namePositionY) { this.namePositionY = namePositionY; }
    public void setFontSize(float fontSize) { this.fontSize = fontSize; }
}
