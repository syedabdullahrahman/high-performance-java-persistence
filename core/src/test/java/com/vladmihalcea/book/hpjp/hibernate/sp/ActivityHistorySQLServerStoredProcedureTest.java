package com.vladmihalcea.book.hpjp.hibernate.sp;

import com.vladmihalcea.book.hpjp.util.AbstractSQLServerIntegrationTest;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * @author Vlad Mihalcea
 */
public class ActivityHistorySQLServerStoredProcedureTest extends AbstractSQLServerIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{};
    }

    @Before
    public void init() {
        super.init();
        ddl("DROP table ACT_HI_PROCINST");
        ddl("DROP table ACT_HI_ACTINST");
        ddl("DROP table ACT_HI_TASKINST");
        ddl("DROP table ACT_HI_VARINST");
        ddl("DROP table ACT_HI_DETAIL");
        ddl("DROP table ACT_HI_COMMENT");
        ddl("DROP table ACT_HI_ATTACHMENT");
        ddl("DROP table ACT_HI_IDENTITYLINK");

        ddl("""
            create table ACT_HI_PROCINST (
                ID_ nvarchar(64) not null,
                PROC_INST_ID_ nvarchar(64) not null,
                BUSINESS_KEY_ nvarchar(255),
                PROC_DEF_ID_ nvarchar(64) not null,
                START_TIME_ datetime not null,
                END_TIME_ datetime,
                DURATION_ numeric(19,0),
                START_USER_ID_ nvarchar(255),
                START_ACT_ID_ nvarchar(255),
                END_ACT_ID_ nvarchar(255),
                SUPER_PROCESS_INSTANCE_ID_ nvarchar(64),
                DELETE_REASON_ nvarchar(4000),
                TENANT_ID_ nvarchar(255) default '',
                NAME_ nvarchar(255),
                primary key (ID_),
                unique (PROC_INST_ID_)
            )
            """);
        ddl("""
            create table ACT_HI_ACTINST (
                ID_ nvarchar(64) not null,
                PROC_DEF_ID_ nvarchar(64) not null,
                PROC_INST_ID_ nvarchar(64) not null,
                EXECUTION_ID_ nvarchar(64) not null,
                ACT_ID_ nvarchar(255) not null,
                TASK_ID_ nvarchar(64),
                CALL_PROC_INST_ID_ nvarchar(64),
                ACT_NAME_ nvarchar(255),
                ACT_TYPE_ nvarchar(255) not null,
                ASSIGNEE_ nvarchar(255),
                START_TIME_ datetime not null,
                END_TIME_ datetime,
                DURATION_ numeric(19,0),
                TENANT_ID_ nvarchar(255) default '',
                primary key (ID_)
            )
            """);
            ddl("""                         
                create table ACT_HI_TASKINST (
                    ID_ nvarchar(64) not null,
                    PROC_DEF_ID_ nvarchar(64),
                    TASK_DEF_KEY_ nvarchar(255),
                    PROC_INST_ID_ nvarchar(64),
                    EXECUTION_ID_ nvarchar(64),
                    NAME_ nvarchar(255),
                    PARENT_TASK_ID_ nvarchar(64),
                    DESCRIPTION_ nvarchar(4000),
                    OWNER_ nvarchar(255),
                    ASSIGNEE_ nvarchar(255),
                    START_TIME_ datetime not null,
                    CLAIM_TIME_ datetime,
                    END_TIME_ datetime,
                    DURATION_ numeric(19,0),
                    DELETE_REASON_ nvarchar(4000),
                    PRIORITY_ int,
                    DUE_DATE_ datetime,
                    FORM_KEY_ nvarchar(255),
                    CATEGORY_ nvarchar(255),
                    TENANT_ID_ nvarchar(255) default '',
                    primary key (ID_)
                )
                """);
            ddl("""                             
                create table ACT_HI_VARINST (
                    ID_ nvarchar(64) not null,
                    PROC_INST_ID_ nvarchar(64),
                    EXECUTION_ID_ nvarchar(64),
                    TASK_ID_ nvarchar(64),
                    NAME_ nvarchar(255) not null,
                    VAR_TYPE_ nvarchar(100),
                    REV_ int,
                    BYTEARRAY_ID_ nvarchar(64),
                    DOUBLE_ double precision,
                    LONG_ numeric(19,0),
                    TEXT_ nvarchar(4000),
                    TEXT2_ nvarchar(4000),
                    CREATE_TIME_ datetime,
                    LAST_UPDATED_TIME_ datetime,
                    primary key (ID_)
                )
                """);
              ddl("""                          
                create table ACT_HI_DETAIL (
                    ID_ nvarchar(64) not null,
                    TYPE_ nvarchar(255) not null,
                    PROC_INST_ID_ nvarchar(64),
                    EXECUTION_ID_ nvarchar(64),
                    TASK_ID_ nvarchar(64),
                    ACT_INST_ID_ nvarchar(64),
                    NAME_ nvarchar(255) not null,
                    VAR_TYPE_ nvarchar(255),
                    REV_ int,
                    TIME_ datetime not null,
                    BYTEARRAY_ID_ nvarchar(64),
                    DOUBLE_ double precision,
                    LONG_ numeric(19,0),
                    TEXT_ nvarchar(4000),
                    TEXT2_ nvarchar(4000),
                    primary key (ID_)
                )
                """);
            ddl("""                            
                create table ACT_HI_COMMENT (
                    ID_ nvarchar(64) not null,
                    TYPE_ nvarchar(255),
                    TIME_ datetime not null,
                    USER_ID_ nvarchar(255),
                    TASK_ID_ nvarchar(64),
                    PROC_INST_ID_ nvarchar(64),
                    ACTION_ nvarchar(255),
                    MESSAGE_ nvarchar(4000),
                    FULL_MSG_ varbinary(max),
                    primary key (ID_)
                )
                """);
             ddl("""                           
                create table ACT_HI_ATTACHMENT (
                    ID_ nvarchar(64) not null,
                    REV_ integer,
                    USER_ID_ nvarchar(255),
                    NAME_ nvarchar(255),
                    DESCRIPTION_ nvarchar(4000),
                    TYPE_ nvarchar(255),
                    TASK_ID_ nvarchar(64),
                    PROC_INST_ID_ nvarchar(64),
                    URL_ nvarchar(4000),
                    CONTENT_ID_ nvarchar(64),
                    TIME_ datetime,
                    primary key (ID_)
                )
                """);
            ddl("""                            
                create table ACT_HI_IDENTITYLINK (
                    ID_ nvarchar(64),
                    GROUP_ID_ nvarchar(255),
                    TYPE_ nvarchar(255),
                    USER_ID_ nvarchar(255),
                    TASK_ID_ nvarchar(64),
                    PROC_INST_ID_ nvarchar(64),
                    primary key (ID_)
                )
                """);

        insertData();

        ddl("create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_)");
        ddl("create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_)");
        ddl("create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_)");
        ddl("create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_)");
        ddl("create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_)");
        ddl("create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_)");
        ddl("create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_)");
        ddl("create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_)");
        ddl("create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_)");
        ddl("create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_)");
        ddl("create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_)");
        ddl("create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_)");
        ddl("create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_)");
        ddl("create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_)");
        ddl("create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_)");
        ddl("create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_)");
        ddl("create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_)");
        ddl("create index ACT_IDX_HI_TASK_INST_PROCINST on ACT_HI_TASKINST(PROC_INST_ID_)");

        ddl("DROP PROCEDURE delete_activity_history_before_date");
        /*ddl(
            "CREATE PROCEDURE delete_activity_history_before_date " +
            "   @postId INT, " +
            "   @commentCount INT OUTPUT " +
            "AS " +
            "BEGIN " +
            "   SELECT @commentCount = COUNT(*)  " +
            "   FROM post_comment  " +
            "   WHERE post_id = @postId " +
            "END"
        );*/
    }

    private final int ACT_HI_PROCINST_ROOT_COUNT = 10;
    private final int ACT_HI_ACTINST_PER_PROC_COUNT = 5;
    private final int ACT_HI_TASKINST_PER_PROC_COUNT = 5;
    private final int ACT_HI_VARINST_PER_TASK_COUNT = 5;
    private final int ACT_HI_DETAIL_PER_TASK_COUNT = 5;
    private final int ACT_HI_COMMENT_PER_TASK_COUNT = 5;
    private final int ACT_HI_ATTACHMENT_PER_TASK_COUNT = 5;
    private final int ACT_HI_IDENTITYLINK_PER_TASK_COUNT = 5;

    private void insertData() {
        doInJPA(entityManager -> {
            int procInstId = 0;
            int procInstRootCount = 0;
            while (procInstRootCount < ACT_HI_PROCINST_ROOT_COUNT) {
                //Add a new root process
                int rootId  = insertProcInst(entityManager, procInstId++, null);
                //Add two child process instances
                int child1Id = insertProcInst(entityManager, procInstId++, rootId);
                int child2Id = insertProcInst(entityManager, procInstId++, rootId);
                //Add two grandchild process instances per child
                insertProcInst(entityManager, procInstId++, child1Id);
                insertProcInst(entityManager, procInstId++, child1Id);
                insertProcInst(entityManager, procInstId++, child2Id);
                insertProcInst(entityManager, procInstId++, child2Id);

                procInstRootCount++;
            }
        });
    }

    private int insertProcInst(EntityManager entityManager, int procId, Integer parentProcId) {
        entityManager.createNativeQuery("""
                    INSERT INTO [ACT_HI_PROCINST] (
                        [ID_],
                        [PROC_INST_ID_],
                        [PROC_DEF_ID_],
                        [SUPER_PROCESS_INSTANCE_ID_],
                        [START_TIME_])
                    VALUES (
                        :id,
                        :proc_inst_id_,
                        'Proc Def',
                        :super_process_instance_id_,
                        :start_time_                    
                    )
                    """)
            .setParameter("id", String.valueOf(procId))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("super_process_instance_id_", parentProcId != null ? String.valueOf(parentProcId) : null)
            .setParameter("start_time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId)))
            .executeUpdate();

        for (int j = 1; j <= ACT_HI_ACTINST_PER_PROC_COUNT; j++) {
            int actId = (procId * 10) + j;
            entityManager.createNativeQuery("""
                        INSERT INTO [ACT_HI_ACTINST] (
                            [ID_],
                            [PROC_INST_ID_],
                            [PROC_DEF_ID_],
                            [EXECUTION_ID_],
                            [ACT_ID_],
                            [ACT_TYPE_],
                            [START_TIME_])
                        VALUES (
                            :id,
                            :proc_inst_id_,
                            1,
                            'Exec Id',
                            'Act Type', 
                            'Act Id', 
                            :start_time_
                        )
                        """)
                .setParameter("id", String.valueOf(actId))
                .setParameter("proc_inst_id_", String.valueOf(actId))
                .setParameter("start_time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(j)))
                .executeUpdate();
        }

        int taskId = (procId * (ACT_HI_ACTINST_PER_PROC_COUNT + 1) * (ACT_HI_TASKINST_PER_PROC_COUNT + 1));

        for (int j = 1; j <= ACT_HI_TASKINST_PER_PROC_COUNT; j++) {
            //Add a new root task
            int rootId  = insertTaskInst(entityManager, procId, taskId++, null);
            //Add two child task instances
            int child1Id = insertTaskInst(entityManager, procId, taskId++, rootId);
            int child2Id = insertTaskInst(entityManager, procId, taskId++, rootId);
            //Add two grandchild task instances per child
            insertTaskInst(entityManager, procId, taskId++, child1Id);
            insertTaskInst(entityManager, procId, taskId++, child1Id);
            insertTaskInst(entityManager, procId, taskId++, child2Id);
            insertTaskInst(entityManager, procId, taskId++, child2Id);
        }

        return procId;
    }

    private int insertTaskInst(EntityManager entityManager, int procId, int taskId, Integer parentTaskId) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_TASKINST] (
                [ID_],
                [PROC_INST_ID_],
                [PARENT_TASK_ID_],
                [START_TIME_])
            VALUES (
                :id,
                :proc_inst_id_,
                :parent_task_id_, 
                :start_time_
            )
            """)
        .setParameter("id", String.valueOf(taskId))
        .setParameter("proc_inst_id_", String.valueOf(procId))
        .setParameter("parent_task_id_", parentTaskId != null ? String.valueOf(parentTaskId) : null)
        .setParameter("start_time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(taskId)))
        .executeUpdate();

        int varId = (taskId * (ACT_HI_VARINST_PER_TASK_COUNT + 1));
        for (int i = 1; i <= ACT_HI_VARINST_PER_TASK_COUNT; i++) {
            insertVarInst(entityManager, procId, taskId, varId++);
        }

        int detailId = (taskId * (ACT_HI_DETAIL_PER_TASK_COUNT + 1));
        for (int i = 1; i <= ACT_HI_DETAIL_PER_TASK_COUNT; i++) {
            insertDetail(entityManager, procId, taskId, detailId++);
        }

        int commentId = (taskId * (ACT_HI_COMMENT_PER_TASK_COUNT + 1));
        for (int i = 1; i <= ACT_HI_COMMENT_PER_TASK_COUNT; i++) {
            insertComment(entityManager, procId, taskId, commentId++);
        }

        int attachmentId = (taskId * (ACT_HI_ATTACHMENT_PER_TASK_COUNT + 1));
        for (int i = 1; i <= ACT_HI_ATTACHMENT_PER_TASK_COUNT; i++) {
            insertAttachment(entityManager, procId, taskId, attachmentId++);
        }

        int identityLinkId = (taskId * (ACT_HI_IDENTITYLINK_PER_TASK_COUNT + 1));
        for (int i = 1; i <= ACT_HI_IDENTITYLINK_PER_TASK_COUNT; i++) {
            insertIdentityLink(entityManager, procId, taskId, identityLinkId++);
        }

        return taskId;
    }

    private int insertVarInst(EntityManager entityManager, int procId, int taskId, int id) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_VARINST] (
                [ID_],
                [PROC_INST_ID_],
                [TASK_ID_],
                [NAME_],
                [CREATE_TIME_])
            VALUES (
                :id,
                :proc_inst_id_,
                :task_id_, 
                :name_, 
                :create_time_
            )
            """)
            .setParameter("id", String.valueOf(id))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("task_id_", taskId)
            .setParameter("name_", String.format("Var: %d", id))
            .setParameter("create_time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(taskId).plusSeconds(id)))
            .executeUpdate();

        return id;
    }

    private int insertDetail(EntityManager entityManager, int procId, int taskId, int id) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_DETAIL] (
                [ID_],
                [TYPE_],
                [PROC_INST_ID_],
                [TASK_ID_],
                [NAME_],
                [TIME_])
            VALUES (
                :id,
                'Type',
                :proc_inst_id_,
                :task_id_, 
                :name_, 
                :time_
            )
            """)
            .setParameter("id", String.valueOf(id))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("task_id_", taskId)
            .setParameter("name_", String.format("Detail: %d", id))
            .setParameter("time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(taskId).plusSeconds(id)))
            .executeUpdate();

        return id;
    }

    private int insertComment(EntityManager entityManager, int procId, int taskId, int id) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_COMMENT] (
                [ID_],
                [PROC_INST_ID_],
                [TASK_ID_],
                [TIME_])
            VALUES (
                :id,
                :proc_inst_id_,
                :task_id_, 
                :time_
            )
            """)
            .setParameter("id", String.valueOf(id))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("task_id_", taskId)
            .setParameter("time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(taskId).plusSeconds(id)))
            .executeUpdate();

        return id;
    }

    private int insertAttachment(EntityManager entityManager, int procId, int taskId, int id) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_ATTACHMENT] (
                [ID_],
                [PROC_INST_ID_],
                [TASK_ID_],
                [TIME_])
            VALUES (
                :id,
                :proc_inst_id_,
                :task_id_, 
                :time_
            )
            """)
            .setParameter("id", String.valueOf(id))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("task_id_", taskId)
            .setParameter("time_", Timestamp.valueOf(LocalDate.of(2020, 12, 25).atStartOfDay().plusHours(procId).plusMinutes(taskId).plusSeconds(id)))
            .executeUpdate();

        return id;
    }

    private int insertIdentityLink(EntityManager entityManager, int procId, int taskId, int id) {
        entityManager.createNativeQuery("""
            INSERT INTO [ACT_HI_IDENTITYLINK] (
                [ID_],
                [PROC_INST_ID_],
                [TASK_ID_])
            VALUES (
                :id,
                :proc_inst_id_,
                :task_id_
            )
            """)
            .setParameter("id", String.valueOf(id))
            .setParameter("proc_inst_id_", String.valueOf(procId))
            .setParameter("task_id_", taskId)
            .executeUpdate();

        return id;
    }

    @Test
    public void testStoredProcedureReturnValue() {
        doInJPA(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            int commentCount = session.doReturningWork(connection -> {
                try (CallableStatement function = connection.prepareCall("{ ? = call fn_count_comments(?) }")) {
                    function.registerOutParameter(1, Types.INTEGER);
                    function.setInt(2, 1);
                    function.execute();
                    return function.getInt(1);
                }
            });
            assertEquals(2, commentCount);
        });
    }
}