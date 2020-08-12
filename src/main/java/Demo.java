import namecaselib.NCL.Gender;
import namecaselib.NCLNameCaseUa;

public class Demo {

    public static void main(String[] args) {
        NCLNameCaseUa nc = new NCLNameCaseUa();
        for (int i = 1; i <= 6; i++) {
            System.out.println(nc.qFirstName("Сергій", i, Gender.MAN));
        }

    }

}
