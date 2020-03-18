package info.subramanya.camerafileupload;

import android.app.Application;

import java.util.ArrayList;

public class ImageList extends Application {

    ArrayList<String> arrayList= new ArrayList<>();


    public ArrayList<String> getArrayList() {
        return arrayList;
    }

    public void setArrayList(String string) {
        this.arrayList.add(string);
    }
}
