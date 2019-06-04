package xu.kevin.pictowar.PictoDB;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class FaceRepository {
    public FaceDAO faceDAO;

    public FaceRepository(Application app){
        FaceDB db = FaceDB.getDatabase(app);
        faceDAO = db.faceDAO();

    }

    public void insertFaceInfo(FaceInfo faceInfo){
        new insertFaceInfo(faceDAO,faceInfo).execute();
    }

    private static class insertFaceInfo extends AsyncTask<Void,Void,Void>{

        private FaceDAO dao;
        private FaceInfo faceInfo;

        public insertFaceInfo(FaceDAO dao, FaceInfo faceInfo){
            this.dao = dao;
            this.faceInfo = faceInfo;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.insertFace(faceInfo);
            return null;
        }


    }

    public FaceInfo getFaceInfo(){

        try {
            List<FaceInfo> temp = new getFaceInfo(faceDAO).execute().get();
            if(temp.size()>0){
                return temp.get(temp.size()-1);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class getFaceInfo extends AsyncTask<Void,Void, List<FaceInfo>>{

        private FaceDAO dao;

        public getFaceInfo(FaceDAO dao){
            this.dao = dao;
        }

        @Override
        protected List<FaceInfo> doInBackground(Void... voids) {

            return dao.getFaceInfo();
        }
    }
}
