package models;

public class Author {
    public Integer id;
    public String authorName;
    public String email;


    public Author(Integer id,String authorName,String email){
        this.id=id;
        this.authorName=authorName;
        this.email=email;
    }


    public Author(){}
}
