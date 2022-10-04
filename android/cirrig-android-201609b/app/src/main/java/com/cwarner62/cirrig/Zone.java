package com.cwarner62.cirrig;

//Class to represent a zone
public class Zone {
    int _id, zoneNumber, irrigCaptureAbility, spacing;
    float containerDiam_in, irrig_in_per_hr, irrig_uniformity, plantHeight_in;
    float plantWidth_in, pctCover, containerSpacing_in, leachingFraction;
    float eto, solar, tmax, tmin, rain;

    //include these two in database to minimize hard coding calculations
    int canopyDensity;
    float plantWidthFactor;

    public static final int SPACING_SQUARE = 0;
    public static final int SPACING_OFFSET = 1;

    public static final int CANOPY_DENSITY_LOW = 0;
    public static final int CANOPY_DENSITY_MED = 1;
    public static final int CANOPY_DENSITY_HIGH = 2;

    public static final int IRRIG_CAPTURE_LOW = 0;
    public static final int IRRIG_CAPTURE_MEDIUM = 1;
    public static final int IRRIG_CAPTURE_HIGH = 2;
    public static final int IRRIG_CAPTURE_NIL = 3;
    public static final int IRRIG_CAPTURE_NEGATIVE = 4;

    public Zone() {
    }

    //create empty zone
    public Zone(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }


    //creates a "default" Zone from basic, hard-coded characteristics
    public Zone(int zoneNumber, float containerDiam, float containerSpacing_in, float plantWidth_in,  float irrig_in_per_hr,float solar, float tmax, float tmin, float rain , float eto) {
        this.zoneNumber = zoneNumber;
        this.containerDiam_in = containerDiam;
        this.plantWidth_in = plantWidth_in;
        plantHeight_in = plantWidth_in;
        irrig_uniformity = 100.0f;
        this.irrigCaptureAbility = IRRIG_CAPTURE_LOW;
        leachingFraction = 10.0f;
        spacing = SPACING_OFFSET;
        canopyDensity = CANOPY_DENSITY_MED;
        float totalPotDiam_in = containerDiam_in + containerSpacing_in;
        pctCover = (plantWidth_in * plantWidth_in) / (totalPotDiam_in * totalPotDiam_in) * getCanopyDensityValue() * 100.0f;
        this.irrig_in_per_hr = irrig_in_per_hr;
        this.solar = solar;
        this.eto = eto;
        this.tmax = tmax;
        this.tmin = tmin;
        this.rain = rain;
    }

    //Create a basic as above, except using user-input plant width
    public Zone(int zoneNumber, float containerDiam, float plantWidth) {
        this.zoneNumber = zoneNumber;
        containerDiam_in = containerDiam;
        plantWidth_in = plantWidth;
        plantHeight_in = plantWidth_in;
        //in percent, 100% = 1.0
        irrig_uniformity = 100.0f;
        irrigCaptureAbility = IRRIG_CAPTURE_LOW;
        //in percent, 10% = 0.1
        leachingFraction = 10.0f;
        containerSpacing_in = 0.0f;
        spacing = SPACING_OFFSET;
        containerSpacing_in = 0;
        canopyDensity = CANOPY_DENSITY_MED;
        //in percent, 100% = 1.0
        float totalPotDiam_in = containerDiam_in + containerSpacing_in;
        pctCover = (plantWidth_in * plantWidth_in) / (totalPotDiam_in * totalPotDiam_in) * getCanopyDensityValue() * 100.0f;
        irrig_in_per_hr = 0.5f;
    }

    //Create a zone from all values in database
    //need to clean out later
    public Zone(int _id, int zoneNumber, int irrigCaptureAbility, int spacing, float containerDiam_in, float irrig_in_per_hr, float irrig_uniformity, float plantHeight_in, float plantWidth_in, float pctCover, float containerSpacing_in, float leachingFraction, int canopyDensity, float plantWidthFactor) {
        this._id = _id;
        this.zoneNumber = zoneNumber;
        this.irrigCaptureAbility = irrigCaptureAbility;
        this.spacing = spacing;
        this.containerDiam_in = containerDiam_in;
        this.irrig_in_per_hr = irrig_in_per_hr;
        this.irrig_uniformity = irrig_uniformity;
        this.plantHeight_in = plantHeight_in;
        this.plantWidth_in = plantWidth_in;
        this.pctCover = pctCover;
        this.containerSpacing_in = containerSpacing_in;
        this.leachingFraction = leachingFraction;
        this.canopyDensity = canopyDensity;
        this.plantWidthFactor = plantWidthFactor;
    }

    public Zone(int _id, int zoneNumber, int irrigCaptureAbility, int spacing, float containerDiam_in, float irrig_in_per_hr, float irrig_uniformity, float plantHeight_in, float plantWidth_in, float pctCover, float containerSpacing_in, float leachingFraction, int canopyDensity, float plantWidthFactor, float eto, float solar, float tmax, float tmin, float rain) {
        this._id = _id;
        this.zoneNumber = zoneNumber;
        this.irrigCaptureAbility = irrigCaptureAbility;
        this.spacing = spacing;
        this.containerDiam_in = containerDiam_in;
        this.irrig_in_per_hr = irrig_in_per_hr;
        this.irrig_uniformity = irrig_uniformity;
        this.plantHeight_in = plantHeight_in;
        this.plantWidth_in = plantWidth_in;
        this.pctCover = pctCover;
        this.containerSpacing_in = containerSpacing_in;
        this.leachingFraction = leachingFraction;
        this.canopyDensity = canopyDensity;
        this.plantWidthFactor = plantWidthFactor;
        this.eto = eto;
        this.solar = solar;
        this.tmax = tmax;
        this.tmin = tmin;
        this.rain = rain;
    }

    /* getters and setters */
    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public int getZoneNumber() {
        return this.zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    public int getIrrigCapture() {
        return this.irrigCaptureAbility;
    }

    public void setIrrigCapture(int irrigCaptureAbility) {
        this.irrigCaptureAbility = irrigCaptureAbility;
    }

    public float getIrrigCaptureValue() {
        switch (irrigCaptureAbility) {
            case IRRIG_CAPTURE_LOW:
                return 1.5f;
            case IRRIG_CAPTURE_MEDIUM:
                return 2.0f;
            case IRRIG_CAPTURE_HIGH:
                return 2.5f;
            case IRRIG_CAPTURE_NEGATIVE:
                return 0.75f;
            case IRRIG_CAPTURE_NIL:
                return 0;
            default:
                return 0;
        }
    }

    public int getSpacing() {
        return this.spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public float getSpacingValue() {
        switch (spacing) {
            case SPACING_SQUARE:
                return 1;
            case SPACING_OFFSET:
                return 0.866f;
            default:
                return 0;
        }
    }

    public float getContainerDiam() {
        return this.containerDiam_in;
    }

    public void setContainerDiam(float containerDiam_in) {
        this.containerDiam_in = containerDiam_in;
    }

    public float getIrrigRate() {
        return this.irrig_in_per_hr;
    }

    public void setIrrigRate(float irrig_in_per_hr) {
        this.irrig_in_per_hr = irrig_in_per_hr;
    }

    public float getIrrigUniformity() {
        return this.irrig_uniformity;
    }

    public void setIrrigUniformity(float irrig_uniformity) {
        this.irrig_uniformity = irrig_uniformity;
    }

    public float getPlantHeight() {
        return this.plantHeight_in;
    }

    public void setPlantHeight(float plantHeight_in) {
        this.plantHeight_in = plantHeight_in;
    }

    public float getPlantWidth() {
        return this.plantWidth_in;
    }

    public int getRoundedPlantWidth() {
        return (int) (this.plantWidth_in + 0.5f);
    }

    public void setPlantWidth(float plantWidth_in) {
        this.plantWidth_in = plantWidth_in;
    }

    public float getPctCover() {
        return this.pctCover;
    }

    public void setPctCover(float pctCover) {
        this.pctCover = pctCover;
    }

    public float getContSpacing() {
        return this.containerSpacing_in;
    }

    public void setContSpacing(float containerSpacing_in) {
        this.containerSpacing_in = containerSpacing_in;
    }

    public float getLeachingFraction() {
        return this.leachingFraction;
    }

    public void setLeachingFraction(float leachingFraction) {
        this.leachingFraction = leachingFraction;
    }

    public int getCanopyDensity() {
        return this.canopyDensity;
    }

    public void setCanopyDensity(int canopyDensity) {
        this.canopyDensity = canopyDensity;
    }

    public float getCanopyDensityValue() {
        switch (canopyDensity) {
            case CANOPY_DENSITY_LOW:
                return 0.5f;
            case CANOPY_DENSITY_MED:
                return 0.75f;
            case CANOPY_DENSITY_HIGH:
                return 1f;
            default:
                return 0;
        }
    }

    public float getPlantWidthFactor() {
        return this.plantWidthFactor;
    }

    public void setPlantWidthFactor(float plantWidthFactor) {
        this.plantWidthFactor = plantWidthFactor;
    }

    //float eto, solar, tmax, tmin, rain
    public float getEto() {
        return this.eto;
    }

    public void setEto(float eto) {
        this.eto = eto;
    }

    public float getSolar() {
        return this.solar;
    }

    public void setSolar(float solar) {
        this.solar = solar;
    }

    public float getTmax() {
        return this.tmax;
    }

    public void setTmax(float tmax) {
        this.tmax = tmax;
    }

    public float getTmin() {
        return this.tmin;
    }

    public void setTmin(float tmin) {
        this.tmin = tmin;
    }

    public float getRain() {
        return this.rain;
    }

    public void setRain(float rain) {
        this.rain = rain;
    }

    //update zone with current settings , using calculate plant width
    public void update(float containerDiam, float contSpacing, int spacingArr, int canopyDensity, float irrig_in_per_hr) {
        //do zone update here
        containerDiam_in = containerDiam;
        containerSpacing_in = contSpacing;
        spacing = spacingArr;
        this.canopyDensity = canopyDensity;
        this.irrig_in_per_hr = irrig_in_per_hr;

        plantWidth_in = containerDiam_in * plantWidthFactor;
        plantHeight_in = plantWidth_in;
        float totalPotDiam_in = containerDiam_in + containerSpacing_in;
        pctCover = (plantWidth_in * plantWidth_in) / (totalPotDiam_in * totalPotDiam_in) * getCanopyDensityValue() * 100.0f;
    }

    //update zone with current settings; user input plant width
    public void updateZone(float containerDiam, float contSpacing, int spacingArr, float plantWidth, int canopyDensity, float irrig_in_per_hr, int irrigCaptureAbility) {
        //do zone update here
        containerDiam_in = containerDiam;
        containerSpacing_in = contSpacing;
        spacing = spacingArr;
        this.canopyDensity = canopyDensity;
        this.irrig_in_per_hr = irrig_in_per_hr;
        this.irrigCaptureAbility = irrigCaptureAbility;

        plantWidth_in = plantWidth;
        plantHeight_in = plantWidth_in;
        float totalPotDiam_in = containerDiam_in + containerSpacing_in;
        pctCover = (plantWidth_in * plantWidth_in) / (totalPotDiam_in * totalPotDiam_in) * getCanopyDensityValue() * 100.0f;
    }

    public static String getSpacingString(int spacing) {
        String[] strs = {"Square", "Offset"};
        if (spacing < 0 || spacing >= strs.length) return "Null";
        return strs[spacing];
    }

    public static String[] getSpacingStrings() {
        String[] strs = {"Square", "Offset"};
        return strs;
    }

    public static String getCanopyDensityString(int density) {
        String[] strs = {"Low", "Med", "High"};
        if (density < 0 || density >= strs.length) return "Null";
        return strs[density];
    }

    public static String[] getCanopyDensityStrings() {
        String[] strs = {"Low", "Med", "High"};
        return strs;
    }

    public static String getIrrigCaptureString(int capture) {
        String[] strs = {"Low", "Med", "High", "Nil", "Negative"};
        if (capture < 0 || capture >= strs.length) return "Null";
        return strs[capture];
    }

    public static String[] getIrrigCaptureStrings() {
        String[] strs = {"Low", "Med", "High", "Nil", "Negative"};
        return strs;
    }

    public static float getIrrigCaptureValue(int irrigCaptureAbility) {
        switch (irrigCaptureAbility) {
            case IRRIG_CAPTURE_LOW:
                return 1.5f;
            case IRRIG_CAPTURE_MEDIUM:
                return 2.0f;
            case IRRIG_CAPTURE_HIGH:
                return 2.5f;
            case IRRIG_CAPTURE_NEGATIVE:
                return 0.75f;
            case IRRIG_CAPTURE_NIL:
                return 0;
            default:
                return 0;
        }
    }

    //for debugging purpose
    public String printAllValue(){

        String listAllValue = "zoneNumber is "+ this.zoneNumber /*+", irrigCaptureAbility is " +this.irrigCaptureAbility*/
                + ", spacing is " + getSpacingValue() + " ,float containerDiam_in is "+ this.containerDiam_in
                + ", irrig_in_per_hr is "+ this.irrig_in_per_hr + " , irrig_uniformity is "+ this.irrig_uniformity
                + " , plantWidth_in is "+ this.plantWidth_in + "containerSpacing_in is "+ this.containerSpacing_in
                + " , eto is "+ this.eto +", solar is "+ this.solar + ", tmax is "+ this.tmax
                + ", tmin is " +this.tmin + " , rain is " + this.rain;

        System.out.println(listAllValue);
        return listAllValue;
    }

}
