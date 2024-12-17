package com.autotest.LiuMa;

import com.autotest.LiuMa.database.domain.TestBackUpFile;
import com.autotest.LiuMa.database.domain.TestFile;
import com.autotest.LiuMa.database.mapper.TestBackUpFileMapper;
import com.autotest.LiuMa.database.mapper.TestFileMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTestBackUpFile {

    @Autowired
    TestBackUpFileMapper testBackUpFileMapper;

    @Autowired
    TestFileMapper testFileMapper;


    @Test
    public void getBackUpFile() {
        List<TestBackUpFile> allTestBackUpFile = testBackUpFileMapper.getAllTestBackUpFile("1");
        System.out.println(allTestBackUpFile);
    }

    @Test
    public void getFile() {
        TestFile testFile = testFileMapper.getTestFile("3fcc0a6bee9c418b986f084a8c817e04");
        System.out.println(testFile);
    }
}
