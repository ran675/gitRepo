package itheima.service;

import com.itheima.GitRepoApplication;
import com.itheima.pojo.Repo;
import com.itheima.service.RepoService;
import com.itheima.utils.POIUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GitRepoApplication.class})
public class RepoServiceTest {

    @Autowired
    private RepoService repoService;

    @Test
    public void batchSave(){
        Workbook workBook = POIUtils.getWorkBook( new File("D:\\Dev\\TerrapinNPMPackageRepositoryKeyUpdate.xlsx"));
        List<Repo> list = POIUtils.readBetweenLines(workBook, 31, 100);
        for (Repo repo : list) {
            System.out.println(repo);
        }
        repoService.saveMsgItemList(list);

    }

}
