package com.ccnode.codegenerator.service.pojo;

import com.ccnode.codegenerator.dialog.InsertDialogResult;
import com.ccnode.codegenerator.dialog.InsertFileProp;
import com.ccnode.codegenerator.dialog.InsertFileType;
import com.ccnode.codegenerator.genCode.GenDaoService;
import com.ccnode.codegenerator.genCode.GenMapperService;
import com.ccnode.codegenerator.genCode.GenServiceService;
import com.ccnode.codegenerator.genCode.GenSqlService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by bruce.ge on 2016/12/26.
 */
public class GenerateInsertCodeService {

    private static Logger logger = LoggerFactory.getLogger(GenerateInsertCodeService.class);

    public static void generateInsert(InsertDialogResult insertDialogResult) {
        Map<InsertFileType, InsertFileProp> fileProps = insertDialogResult.getFileProps();
        ExecutorService executorService = Executors.newFixedThreadPool(fileProps.size());
        List<Future> futures = Lists.newArrayList();
        for (InsertFileType fileType : fileProps.keySet()) {
            Future<Void> future = executorService.submit((Callable<Void>) () -> {
                generateFiles(fileType, fileProps, insertDialogResult);
                return null;
            });
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }


    private static void generateFiles(InsertFileType type, Map<InsertFileType, InsertFileProp> propMap, InsertDialogResult insertDialogResult) {
        switch (type) {
            case SQL: {
                GenSqlService.generateSqlFile(propMap.get(type), insertDialogResult.getPropList(), insertDialogResult.getPrimaryProp(), insertDialogResult.getTableName());
                break;
            }
            case DAO: {
                GenDaoService.generateDaoFileUsingFtl(propMap.get(type), insertDialogResult.getSrcClass());
                break;
            }
            case MAPPER_XML: {
                GenMapperService.generateMapperXmlUsingFtl(propMap.get(type), insertDialogResult.getPropList(), insertDialogResult.getSrcClass(), propMap.get(InsertFileType.DAO), insertDialogResult.getTableName(), insertDialogResult.getPrimaryProp());
                break;
            }
            case SERVICE: {
                GenServiceService.generateServiceUsingFtl(propMap.get(type), insertDialogResult.getSrcClass(), propMap.get(InsertFileType.DAO));
                break;
            }
        }
    }


}
