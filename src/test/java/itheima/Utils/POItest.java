package itheima.Utils;

import com.itheima.pojo.Repo;
import com.itheima.utils.POIUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={POItest.class})
public class POItest {

    @Test
    public void POIt(){
        Workbook workBook = POIUtils.getWorkBook( new File("D:\\Dev\\TerrapinNPMPackageRepositoryKeyUpdate.xlsx"));
        List<Repo> list = POIUtils.readBetweenLines(workBook, 31, 100);
        for (Repo repo : list) {
            System.out.println(repo);
        }
    }

}
