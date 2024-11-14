package com.autotest.LiuMa.controller;

import com.autotest.LiuMa.common.utils.PageUtils;
import com.autotest.LiuMa.common.utils.Pager;
import com.autotest.LiuMa.database.domain.Element;
import com.autotest.LiuMa.dto.ElementDTO;
import com.autotest.LiuMa.dto.ElementImportDTO;
import com.autotest.LiuMa.request.QueryRequest;
import com.autotest.LiuMa.service.ElementService;
import com.excel.poi.ExcelBoot;
import com.excel.poi.entity.ErrorEntity;
import com.excel.poi.function.ImportFunction;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/autotest/element")
public class ElementController {

    @Resource
    private ElementService elementService;

    @PostMapping("/save")
    public void saveElement(@RequestBody Element element, HttpServletRequest request) {
        String user = request.getSession().getAttribute("userId").toString();
        element.setUpdateUser(user);
        elementService.saveElement(element);
    }

    @PostMapping("/delete")
    public void deleteElement(@RequestBody Element element) {
        elementService.deleteElement(element);
    }

    @PostMapping("/list/module")
    public List<Element> getElementDetail(@RequestBody QueryRequest request){
        return elementService.getModuleElementList(request.getProjectId(), request.getModuleId());
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ElementDTO>> getElementList(@PathVariable int goPage, @PathVariable int pageSize,
                                          @RequestBody QueryRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, elementService.getElementList(request));
    }


    @PostMapping("/import")
    public String element_import(MultipartFile file , @RequestParam String projectId, @RequestParam String moduleId, HttpServletRequest request) throws IOException {
//        InputStream inputStream = file.getInputStream();
//        ExcelReader reader = ExcelUtil.getReader(inputStream);
//        HashMap<String, String> headerAlias = new HashMap<>(5);
//        headerAlias.put("元素名称(必填)","name");
//        headerAlias.put("定位方式(必填)","by");
//        headerAlias.put("表达式(必填)","expression");
////        headerAlias.put("所属页面(必填)","modulePath");
//        headerAlias.put("元素描述","description");
//        reader.setHeaderAlias(headerAlias);
//        List<ElementDTO> list = reader.readAll(ElementDTO.class);
//        System.out.println(list);
//        System.out.println(projectId);
//        System.out.println(moduleId);
//        System.out.println(request.getSession().getAttribute("userId").toString());
//        String userId = request.getSession().getAttribute("userId").toString();
//        elementService.importElement(list,projectId,moduleId,userId);

        List<Object> success_list = new ArrayList<>();
        List<Object> fail_list = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        String user = request.getSession().getAttribute("userId").toString();
        ExcelBoot.ImportBuilder(file.getInputStream(),ElementImportDTO.class).importExcel(new ImportFunction<ElementImportDTO>() {
            @Override
            public void onProcess(int sheetIndex,  int rowIndex,  ElementImportDTO element) {
                element.setUpdateUser(user);
                element.setModuleId(moduleId);
                element.setProjectId(projectId);
                elementService.importElement(element);
                success_list.add(1);
            }

            @Override
            public void onError(ErrorEntity errorEntity) {
                fail_list.add(1);
                buffer.append(errorEntity.getErrorMessage() + "\n");
            }
        });
        return fail_list.size()==0?"导入结果: 成功 ".concat(String.valueOf(success_list.size())).concat(" 条 "):"导入结果: 成功 ".concat(String.valueOf(success_list.size())).concat(" 条 ，失败 ").concat(String.valueOf(fail_list.size())).concat(" 条 ，\n失败原因：")
                .concat(buffer.toString());
    }



//    下载导入模板功能

    /**
     * @param response
     * @功能描述 下载文件:
     */
    @GetMapping("/download_template")
    public void download_template( HttpServletResponse response) throws IOException {
//        // 读到流中
//        String path = "target/classes/download_template/element_import_template.xlsx";
//        InputStream inputStream = new FileInputStream(path);// 文件的存放路径
//        response.reset();
//        response.setContentType("application/octet-stream");
//        String filename = new File(path).getName();
//        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
//        ServletOutputStream outputStream = response.getOutputStream();
//        byte[] b = new byte[1024];
//        int len;
////从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
//        while ((len = inputStream.read(b)) > 0) {
//            outputStream.write(b, 0, len);
//        }
//        inputStream.close();

        ExcelBoot.ExportBuilder(response, "ElementTemplate", ElementImportDTO.class).exportTemplate();


    }




}
