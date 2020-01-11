class JobCompletion{
    private String worker;
    private String image;
    private long tsCreationMessage;
    private long tsReceptionWorker;
    private long tsFinalizationWorker;
    // Constructor
    // Setters & getters
    
    public void setWorker(String w){
        worker = w;
    }
    
    public String getWorker(){
        return worker;
    }
    
    public void setImage(String i){
        image = i;
    }

    public String getImage(){
        return image;
    }
    
    public void setTsCreationMessage(long cm){
        tsCreationMessage = cm;
    }

    public long getTsCreationMessage(){
        return tsCreationMessage;
    }
    
    public void setTsReceptionWorker(long rw){
        tsReceptionWorker = rw;
    }

    public long getTsReceptionWorker(){
        return tsReceptionWorker;
    }
    
    public void setTsFinalizationWorker(long fw){
        tsFinalizationWorker = fw;
    }

    public long getTsFinalizationWorker(){
        return tsFinalizationWorker;
    }
}
