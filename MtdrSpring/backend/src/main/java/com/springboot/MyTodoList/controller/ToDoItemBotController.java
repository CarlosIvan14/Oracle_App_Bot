package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.SimplifiedTaskDTO;
import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.service.ProjectsServiceBot;
import com.springboot.MyTodoList.service.SprintsServiceBot;
import com.springboot.MyTodoList.service.TaskCreationServiceBot;
import com.springboot.MyTodoList.service.TaskServiceBot;
import com.springboot.MyTodoList.service.UserRoleServiceBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ToDoItemBotController extends TelegramLongPollingBot {

    /* ========================================================= */
    /* ======================= FIELDS =========================== */
    /* ========================================================= */
    private static final Logger log = LoggerFactory.getLogger(ToDoItemBotController.class);

    private final ProjectsServiceBot     projectsSvc;
    private final SprintsServiceBot      sprintsSvc;
    private final TaskServiceBot         taskSvc;
    private final TaskCreationServiceBot taskCreationSvc;
    private final UserRoleServiceBot     roleSvc;

    private final RestTemplate rest =
            new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    private final String baseUrl;
    private final String botName;

    /* ======================   STATE  ========================== */
    private enum Flow { NONE, LOGIN, ADD_SPRINT, ADD_TASK, ADD_USER, TASK_COMPLETE, REPORTS }

    private static class ChatState {
        Flow flow = Flow.NONE;
        int  step = 0;

        OracleUser loggedUser;
        int  currentProjectId;
        int  currentSprintId;
        int  currentTaskId;

        /* login */
        String tmpUser; String tmpPass;

        /* add‑sprint */
        String newSprintName;

        /* add‑task */
        String tName, tDesc;
        LocalDate tDeadline; int tSP; double tEst;
        String mode;               // FREE | ASSIGN | AI
        Integer assigneeUserId;

        /* add‑user */
        List<OracleUser> oracleUsers;

        /* reports */
        String rFilter; String rDateOrSprint; String rMemberId; String rUserId;

        /* telegram */
        Long telegramId; String phone;
    }
    private final Map<Long,ChatState> chats = new HashMap<>();

    /* ===================== CONSTRUCTOR ======================== */
    public ToDoItemBotController(String botToken,
                                 String botName,
                                 String backendBaseUrl,
                                 ProjectsServiceBot     projectsSvc,
                                 SprintsServiceBot      sprintsSvc,
                                 TaskServiceBot         taskSvc,
                                 TaskCreationServiceBot taskCreationSvc,
                                 UserRoleServiceBot     roleSvc) {

        super(botToken);
        this.botName         = botName;
        this.baseUrl         = backendBaseUrl;
        this.projectsSvc     = projectsSvc;
        this.sprintsSvc      = sprintsSvc;
        this.taskSvc         = taskSvc;
        this.taskCreationSvc = taskCreationSvc;
        this.roleSvc         = roleSvc;
    }
    @Override public String getBotUsername() { return botName; }

    /* ========================================================= */
    /* ====================== UPDATE =========================== */
    /* ========================================================= */
    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long   chatId = update.getMessage().getChatId();
        String txt    = update.getMessage().getText().trim();

        ChatState st = chats.containsKey(chatId) ? chats.get(chatId)
                                                 : createState(chatId);

        /* --- contacto opcional --- */
        if (update.getMessage().hasContact()) {
            st.telegramId = update.getMessage().getContact().getUserId();
            st.phone      = update.getMessage().getContact().getPhoneNumber();
        } else {
            st.telegramId = update.getMessage().getFrom().getId();
        }

        /* ---------- comandos ---------- */
        if ("/start".equalsIgnoreCase(txt))                   { startLogin(chatId, st); return; }
        if ("/logout".equalsIgnoreCase(txt) || "Logout 🚪".equals(txt))
                                                             { logout(chatId); return; }
        if ("/menu".equalsIgnoreCase(txt)) {
            if (st.loggedUser!=null) showMainMenu(chatId, st.loggedUser);
            else send(chatId,"No hay sesión. /start",false);
            return;
        }

        /* ---------- navegación simple ---------- */
        if (navigate(chatId, txt, st)) return;

        /* ---------- project / sprint ---------- */
        if (selectProject(chatId, txt, st)) return;
        if (selectSprint (chatId, txt, st)) return;

        /* ---------- botones de sprint ---------- */
        if ("📋 Todas las Tareas".equals(txt)) { showAllTasks(chatId, st.currentSprintId); return; }
        if ("➕ Add Task".equals(txt))          { startAddTask(chatId, st);               return; }
        if (handleTaskButtons(chatId,txt,st))  return;
        if (txt.startsWith("👤 ASSIGN-"))      { handleAssign(chatId,txt,st);             return; }

        /* ---------- admin ---------- */
        if (txt.startsWith("👥 Ver Usuarios Proyecto")) {
            int pid = Integer.parseInt(txt.substring(txt.lastIndexOf(' ')+1));
            showUsers(chatId, pid); return;
        }
        if ("➕ Agregar Usuario".equals(txt)) { st.flow=Flow.ADD_USER; st.step=1; listOracleUsers(chatId,st); return; }
        if ("➕ Añadir Sprint".equals(txt))  { st.flow=Flow.ADD_SPRINT; st.step=1; send(chatId,"Nombre del nuevo sprint:",false); return; }
        if (txt.startsWith("Deshabilitar-")) { toggleSprint(chatId,Integer.parseInt(txt.substring(13)),"idle",st); return; }
        if (txt.startsWith("Habilitar-"))    { toggleSprint(chatId,Integer.parseInt(txt.substring(10)),"Active",st); return; }

        /* ---------- reports ---------- */
        if ("📊 Reports".equals(txt)) { st.flow=Flow.REPORTS; st.step=1;
            send(chatId,"Filtro:\n1️⃣ sprint\n2️⃣ week\n3️⃣ month\nEnvia 1/2/3",false); return; }

        /* ---------- flujo activo ---------- */
        if (st.loggedUser==null && st.flow!=Flow.LOGIN){ startLogin(chatId,st); return; }
        if (st.flow!=Flow.NONE)                       { processFlow(chatId,txt,st); return; }
    }
    /**
 * Gestiona los botones de navegación “⬅️ Volver a …”.
 *
 * @return true  si el texto recibido era uno de los botones de navegación
 *         false si no coincidió con ninguno
 */
    private boolean navigate(long chatId, String txt, ChatState st) {

        /* ← Volver al listado de proyectos */
        if ("⬅️ Volver a Proyectos".equals(txt) ||
            "⬅️ Regresar a Proyectos".equals(txt)) {

            showMainMenu(chatId, st.loggedUser);
            return true;
        }

        /* ← Volver al listado de sprints del proyecto actual */
        if ("⬅️ Volver a Sprints".equals(txt)) {
            boolean isManager = roleSvc.isManagerInProject(
                                    st.currentProjectId,
                                    st.loggedUser.getIdUser());
            showSprintsForProject(chatId, st.currentProjectId, isManager);
            return true;
        }

        /* No era navegación */
        return false;
    }

    /**  Decide qué sub‑flujo ejecutar (login, add‑task, etc.).  */
    private void processFlow(long chatId,
        String txt,
        ChatState st) {

        switch (st.flow) {

        case LOGIN:
        loginFlow(chatId, txt, st);
        break;

        case ADD_SPRINT:
        sprintFlow(chatId, txt, st);
        break;

        case ADD_TASK:
        addTaskFlow(chatId, txt, st);
        break;

        case ADD_USER:
        addUserFlow(chatId, txt, st);
        break;

        case TASK_COMPLETE:
        completeFlow(chatId, txt, st);
        break;

        case REPORTS:
        reportsFlow(chatId, txt, st);
        break;

        default:
        // nada
        break;
        }
        }

    /* ========================================================= */
    /* ================= FLUJO LOGIN =========================== */
    /* ========================================================= */
    private void startLogin(long chatId, ChatState st){
        st.flow = Flow.LOGIN; st.step = 1;

        SendMessage m = new SendMessage(String.valueOf(chatId),
                "¡Bienvenido! Ingresa tu *usuario* o comparte tu contacto.");
        m.enableMarkdown(true);

        KeyboardButton b   = new KeyboardButton("Compartir Contacto");
        b.setRequestContact(true);
        KeyboardRow   row  = new KeyboardRow(); row.add(b);
        ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup();
        kb.setResizeKeyboard(true);
        kb.setKeyboard(Arrays.asList(row));

        m.setReplyMarkup(kb);
        exec(m);
    }
    private void loginFlow(long chatId,String txt,ChatState st){
        if (st.step==1){
            st.tmpUser = txt; st.step=2;
            send(chatId,"Password:",false); return;
        }
        if (st.step==2){
            st.tmpPass = txt;
            OracleUser u = doLogin(st.tmpUser, st.tmpPass);
            if (u==null){ send(chatId,"Login fallido.",false); reset(st); return; }

            /* patch telegram */
            HashMap<String,Object> p = new HashMap<String,Object>();
            p.put("telegramId", st.telegramId);
            p.put("phoneNumber", st.phone);
            rest.exchange(baseUrl+"/users/"+u.getIdUser(),
                          HttpMethod.PATCH,new HttpEntity<HashMap<String,Object>>(p),OracleUser.class);

            st.loggedUser=u; reset(st);
            send(chatId,"¡Hola "+u.getName()+"!",false);
            showMainMenu(chatId,u);
        }
    }

    /* ========================================================= */
    /* =============   OTROS SUB‑FLOWS (sprint, task …) ======== */
    /* ========================================================= */
    private void sprintFlow(long c,String txt,ChatState st){
        if (st.step==1){
            st.newSprintName = txt; st.step=2;
            send(c,"/confirmar para guardar ó /cancel",false); return;
        }

        if (st.step==2){
            if ("/confirmar".equalsIgnoreCase(txt)){
                Sprint s=new Sprint();
                s.setName(st.newSprintName); s.setDescription("Active");
                Projects p=new Projects(); p.setIdProject(st.currentProjectId); s.setProject(p);
                rest.postForObject(baseUrl+"/api/sprints",s,Sprint.class);
                send(c,"Sprint creado.",false);
            }else send(c,"Cancelado.",false);

            boolean mgr = roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
            showSprintsForProject(c,st.currentProjectId,mgr); reset(st);
        }
    }

    /* -------------  flujo add‑task (sin flechas) ------------- */
    private void startAddTask(long chatId, ChatState st){
        st.flow=Flow.ADD_TASK; st.step=1;
        boolean mgr=roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
        st.mode = mgr? "FREE":"ASSIGN";
        send(chatId,"🆕 Task\n1) Nombre:",false);
    }
    private void addTaskFlow(long chatId,String txt,ChatState st){

        switch(st.step){

            case 1:
                st.tName=txt; st.step=2;
                send(chatId,"2) Descripción:",false);
                break;

            case 2:
                st.tDesc=txt; st.step=3;
                send(chatId,"3) Deadline (YYYY-MM-DD):",false);
                break;

            case 3:
                try{ st.tDeadline = LocalDate.parse(txt); }
                catch(DateTimeParseException e){ send(chatId,"Fecha inválida",false);return; }
                st.step=4; send(chatId,"4) Story Points:",false);
                break;

            case 4:
                try{ st.tSP=Integer.parseInt(txt); }
                catch(NumberFormatException e){ send(chatId,"Número",false);return; }
                st.step=5; send(chatId,"5) Horas estimadas (e.g. 2.5):",false);
                break;

            case 5:
                try{ st.tEst=Double.parseDouble(txt); }
                catch(NumberFormatException e){ send(chatId,"Número",false);return; }

                boolean mgr=roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
                if(!mgr){ createTask(chatId,st); return; }

                st.step=6;
                send(chatId,"6) Tipo:\n1) Free\n2) Asignar usuario\n3) IA",false);
                break;

            case 6:
                if("1".equals(txt)){ st.mode="FREE"; createTask(chatId,st); return; }
                if("2".equals(txt)){
                    st.mode="ASSIGN";
                    st.oracleUsers=getProjectUsers(st.currentProjectId);
                    StringBuilder sb=new StringBuilder("Usuario:\n");
                    for(int i=0;i<st.oracleUsers.size();i++)
                        sb.append(i+1).append(") ").append(st.oracleUsers.get(i).getName()).append("\n");
                    st.step=7; send(chatId,sb.toString(),false); return;
                }
                if("3".equals(txt)){
                    st.mode="AI";
                    HashMap<String,Object> payload=new HashMap<String,Object>();
                    payload.put("projectId",st.currentProjectId);
                    payload.put("name",st.tName);
                    payload.put("description",st.tDesc);
                    OracleUser[] rec = rest.postForEntity(baseUrl+"/assignment/by-ai",payload,OracleUser[].class).getBody();
                    if(rec!=null&&rec.length>0){
                        st.assigneeUserId=rec[0].getIdUser();
                        st.step=8;
                        send(chatId,"IA sugiere *"+rec[0].getName()+"*\nEscribe OK para aceptar u otro ID:",true);
                    }else{
                        st.mode="FREE"; createTask(chatId,st);
                    }
                    return;
                }
                send(chatId,"Solo 1/2/3",false);
                break;

            case 7:
                int idx;
                try{ idx=Integer.parseInt(txt);}catch(Exception e){ send(chatId,"Número",false);return; }
                if(idx<1||idx>st.oracleUsers.size()){ send(chatId,"Fuera de rango",false);return; }
                st.assigneeUserId=st.oracleUsers.get(idx-1).getIdUser();
                createTask(chatId,st);
                break;

            case 8:
                if(!"OK".equalsIgnoreCase(txt)){
                    try{ st.assigneeUserId=Integer.parseInt(txt);}
                    catch(Exception e){ send(chatId,"Número",false);return; }
                }
                createTask(chatId,st);
                break;

            default: break;
        }
    }
    private void createTask(long chatId,ChatState st){
        try{
            Tasks t=new Tasks();
            t.setName(st.tName); t.setDescription(st.tDesc);
            t.setDeadline(st.tDeadline.atStartOfDay());
            t.setStoryPoints(st.tSP); t.setEstimatedHours(st.tEst);
            t.setRealHours(0.0); t.setCreationTs(LocalDateTime.now());
            Sprint s=new Sprint(); s.setId(st.currentSprintId); t.setSprint(s);

            boolean mgr=roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
            t.setStatus(mgr? ("FREE".equals(st.mode)?"UNASSIGNED":"ASSIGNED"):"ASSIGNED");

            Tasks created = taskCreationSvc.createTask(t);

            if(!mgr){
                Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
                taskCreationSvc.assignTask(created.getId(), pu);
            }else if(!"FREE".equals(st.mode)){
                Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.assigneeUserId);
                taskCreationSvc.assignTask(created.getId(), pu);
            }

            send(chatId,"✅ Task creada.",false);
        }catch(Exception e){
            log.error("Error creando task",e);
            send(chatId,"Error creando task",false);
        }finally{
            reset(st);
            Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
            listTasksForSprint(chatId,st.currentSprintId,pu);
        }
    }

    /* ========================================================= */
    /* =============   COMPLETE, REPORTS, BOTONES  ============= */
    /* ========================================================= */
    private void completeFlow(long c,String txt,ChatState st){
        try{
            double hrs=Double.parseDouble(txt);
            HashMap<String,Object> m=new HashMap<String,Object>();
            m.put("status","COMPLETED"); m.put("realHours",hrs);
            taskSvc.updateTask(st.currentTaskId,m);
            Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
            listTasksForSprint(c,st.currentSprintId,pu); reset(st);
        }catch(Exception e){ send(c,"Número?",false); }
    }

    private void reportsFlow(long chatId,String txt,ChatState st){
        switch(st.step){

            case 1:
                if("1".equals(txt))      st.rFilter="sprint";
                else if("2".equals(txt)) st.rFilter="week";
                else if("3".equals(txt)) st.rFilter="month";
                else{ send(chatId,"1/2/3",false); return; }
                st.step=2;
                send(chatId, st.rFilter.equals("sprint")?"ID del sprint:":"Fecha (yyyy-MM-dd):",false);
                break;

            case 2:
                st.rDateOrSprint=txt.trim(); st.step=3;
                List<OracleUser> us=getProjectUsers(st.currentProjectId);
                st.oracleUsers=us;
                StringBuilder sb=new StringBuilder("0) Todo el equipo\n");
                for(int i=0;i<us.size();i++) sb.append(i+1).append(") ").append(us.get(i).getName()).append("\n");
                send(chatId,sb.toString(),false);
                break;

            case 3:
                int idx;
                try{ idx=Integer.parseInt(txt);}catch(Exception e){ send(chatId,"Número",false);return; }
                if(idx==0) st.rMemberId="all";
                else if(idx > 0 && idx <= st.oracleUsers.size()) {
                    OracleUser selectedUser = st.oracleUsers.get(idx - 1);
                    
                    Integer userId = selectedUser.getIdUser(); // Synchronous call
                    System.out.println("Selected user index: " + idx + ", User ID: " + userId);
                    
                    String puIdEpt = baseUrl+"/api/project-users/project-id/"+st.currentProjectId+"/user-id/"+userId;
                    Integer projectUserId=Optional.ofNullable(rest.getForObject(puIdEpt,Integer.class)).orElse(0);
                    System.out.println("Project ID: " + st.currentProjectId + ", Project User ID: " + projectUserId);
                
                    st.rUserId = String.valueOf(userId);
                    st.rMemberId = String.valueOf(projectUserId);
                    System.out.println("st.rMemberId set to: " + st.rMemberId);
                }                
                else { send(chatId,"Fuera de rango",false); return; }
                sendReport(chatId,st); reset(st);
                break;

            default: break;
        }
    }

    private void sendReport(long chatId,ChatState st){
        boolean team="all".equals(st.rMemberId);
        String pre=baseUrl+"/api/task-assignees/";
        String cntEpt,dataEpt;

        if("sprint".equals(st.rFilter)){
            String s=st.rDateOrSprint;
            cntEpt = team? pre+"team-sprint/"+s+"/done/count"
                          : pre+"user/"+st.rMemberId+"/sprint/"+s+"/done/count";
            dataEpt= team? pre+"team-sprint/"+s+"/done"
                          : pre+"user/"+st.rMemberId+"/sprint/"+s+"/done";
        }else if("week".equals(st.rFilter)){
            String d=st.rDateOrSprint;
            cntEpt = team? pre+"team-week/"+d+"/project/"+st.currentProjectId+"/done/count"
                          : pre+"user/"+st.rMemberId+"/week/"+d+"/done/count";
            dataEpt= team? pre+"team-week/"+d+"/project/"+st.currentProjectId+"/done"
                          : pre+"user/"+st.rMemberId+"/week/"+d+"/done";
        }else{
            String d=st.rDateOrSprint;
            cntEpt = team? pre+"team-month/"+d+"/project/"+st.currentProjectId+"/done/count"
                          : pre+"user/"+st.rMemberId+"/month/"+d+"/done/count";
            dataEpt= team? pre+"team-month/"+d+"/project/"+st.currentProjectId+"/done"
                          : pre+"user/"+st.rMemberId+"/month/"+d+"/done";
        }

        int done=Optional.ofNullable(rest.getForObject(cntEpt,Integer.class)).orElse(0);
        TaskAssignees[] arr=rest.getForObject(dataEpt,TaskAssignees[].class);

        double est=0,real=0;
        if(arr!=null) for(TaskAssignees ta:arr){
            est+=ta.getTask().getEstimatedHours();
            real+=ta.getTask().getRealHours()==null?0:ta.getTask().getRealHours();
        }
        String who=team?"Todo el equipo":
                st.oracleUsers.stream().filter(u->u.getIdUser()==Integer.parseInt(st.rUserId))
                               .findFirst().map(OracleUser::getName).orElse("—");

        StringBuilder sb=new StringBuilder("*Reporte*\n");
        sb.append("Miembro: ").append(who).append("\n")
          .append("Completadas: ").append(done).append("\n")
          .append("Horas est.: ").append(est).append("\n")
          .append("Horas reales: ").append(real);

        send(chatId,sb.toString(),true);
    }

    /* ========================================================= */
    /* =============== BOTONES DE TABLERO ====================== */
    /* ========================================================= */
    private boolean handleTaskButtons(long chatId,String txt,ChatState st){
        if(txt.startsWith("▶ START-")){ changeStatus(chatId,txt,"IN_PROGRESS",st); return true; }
        if(txt.startsWith("❌ CANCEL-")){ changeStatus(chatId,txt,"ASSIGNED",st);   return true; }
        if(txt.startsWith("↩ UNDO-"  )){ changeStatus(chatId,txt,"IN_PROGRESS",st); return true; }
        if(txt.startsWith("✅ DONE-"  )){
            st.flow=Flow.TASK_COMPLETE; st.step=1; st.currentTaskId=id(txt);
            send(chatId,"Horas reales?",false); return true; }
        return false;
    }
    private void changeStatus(long chatId,String txt,String status,ChatState st){
        HashMap<String,Object> m=new HashMap<String,Object>();
        m.put("status",status);
        taskSvc.updateTask(id(txt),m);
        Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
        listTasksForSprint(chatId,st.currentSprintId,pu);
    }
    /**
 * Cierra la sesión del usuario en este chat, borra el estado
 * de la conversación y quita el teclado permanente.
 */
    private void logout(long chatId) {
        // 1) Elimina el estado que guardamos en el Map
        chats.remove(chatId);

        // 2) Construye el mensaje de despedida
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Sesión cerrada. Usa /start para entrar de nuevo.");

        // 3) Quita el teclado (ReplyKeyboardRemove)
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        msg.setReplyMarkup(remove);

        // 4) Envía
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Error enviando logout", e);
        }
    }

    private void handleAssign(long chatId,String txt,ChatState st){
        int taskId=id(txt);
        Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
        if(pu==null){ send(chatId,"No se pudo asignar.",false); return; }
        taskCreationSvc.assignTask(taskId,pu);
        HashMap<String,Object> m=new HashMap<String,Object>();
        m.put("status","ASSIGNED");
        taskSvc.updateTask(taskId,m);
        listTasksForSprint(chatId,st.currentSprintId,pu);
        send(chatId,"Asignada",false);
    }

    /* ========================================================= */
    /* ================  LISTADOS  ============================= */
    /* ========================================================= */
    private void showMainMenu(long chatId,OracleUser user){

        List<Projects> projs = projectsSvc.getProjectsByUserId(user.getIdUser());

        ReplyKeyboardMarkup kb=new ReplyKeyboardMarkup(); kb.setResizeKeyboard(true);
        List<KeyboardRow> rows=new ArrayList<KeyboardRow>();

        rows.add(row("== Proyectos Asignados =="));

        if(projs.isEmpty()) rows.add(row("Sin proyectos"));
        else for(Projects p:projs)
                rows.add(row("📁 "+p.getName()+" (ID: "+p.getIdProject()+")"));

        rows.add(row("Logout 🚪"));
        kb.setKeyboard(rows);
        send(chatId,"Menú:",kb);
    }

    private boolean selectProject(long chatId,String txt,ChatState st){
        Matcher m=Pattern.compile("\\(ID: (\\d+)\\)").matcher(txt);
        if(!m.find()) return false;
        st.currentProjectId=Integer.parseInt(m.group(1));
        boolean mgr=roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
        showSprintsForProject(chatId,st.currentProjectId,mgr);
        return true;
    }
    private boolean selectSprint(long chatId,String txt,ChatState st){
        if(!txt.contains("#SPRINT#")) return false;
        Matcher m=Pattern.compile("\\(ID: (\\d+)\\) #SPRINT#").matcher(txt);
        if(!m.find()) return false;
        st.currentSprintId=Integer.parseInt(m.group(1));
        Integer pu=taskCreationSvc.getProjectUserId(st.currentProjectId, st.loggedUser.getIdUser());
        listTasksForSprint(chatId,st.currentSprintId,pu);
        return true;
    }

    private void showSprintsForProject(long chatId,int projectId,boolean mgr){
        List<Sprint> sprints=sprintsSvc.getSprintsByProjectId(projectId);
        ReplyKeyboardMarkup kb=new ReplyKeyboardMarkup(); kb.setResizeKeyboard(true);
        List<KeyboardRow> rows=new ArrayList<KeyboardRow>();

        rows.add(row("⬅️ Volver a Proyectos"));
        if(mgr){
            rows.add(row("👥 Ver Usuarios Proyecto "+projectId));
            rows.add(row("➕ Añadir Sprint"));
        }
        rows.add(row("📊 Reports"));
        rows.add(row("Sprints del Proyecto "+projectId));

        for(Sprint s:sprints){
            if(!mgr && !"Active".equalsIgnoreCase(s.getDescription())) continue;
            String icon="Active".equalsIgnoreCase(s.getDescription())?"🟢":"🔴";
            KeyboardRow r=new KeyboardRow();
            r.add(icon+" "+s.getName()+" (ID: "+s.getId()+") #SPRINT#");
            if(mgr){
                String toggle=("Active".equalsIgnoreCase(s.getDescription())?"Deshabilitar-":"Habilitar-")+s.getId();
                r.add(toggle);
            }
            rows.add(r);
        }
        kb.setKeyboard(rows);
        send(chatId,"Selecciona sprint:",kb);
    }

    private void listTasksForSprint(long chatId,int sprintId,int projectUserId){

        List<TaskAssignees> assigns = taskSvc.getUserTaskAssignments(sprintId,projectUserId);
        List<Tasks> myTasks = assigns.stream().map(TaskAssignees::getTask).collect(Collectors.toList());
        List<SimplifiedTaskDTO> unassigned = taskSvc.getUnassignedTasksBySprint(sprintId);

        List<Tasks> assigned = filterByStatus(myTasks,"ASSIGNED");
        List<Tasks> prog     = filterByStatus(myTasks,"IN_PROGRESS");
        List<Tasks> done     = filterByStatus(myTasks,"COMPLETED");

        ReplyKeyboardMarkup kb=new ReplyKeyboardMarkup(); kb.setResizeKeyboard(true);
        List<KeyboardRow> rows=new ArrayList<KeyboardRow>();

        rows.add(row("📋 Todas las Tareas"));
        rows.add(row("⬅️ Volver a Sprints"));
        rows.add(row("➕ Add Task"));

        /* unassigned */
        if(!unassigned.isEmpty()){
            rows.add(titleRow("==📭 NO ASIGNADAS 📭=="));
            for(SimplifiedTaskDTO t:unassigned){
                KeyboardRow r=new KeyboardRow();
                r.add(t.getDescription()+" [ID: "+t.getId()+"]");
                r.add("👤 ASSIGN-"+t.getId());
                rows.add(r);
            }
        }

        /* assigned */
        if(!assigned.isEmpty()){
            rows.add(titleRow("==📥 ASIGNADAS 📥=="));
            for(Tasks t:assigned) rows.add(row(t.getDescription()+" [ID: "+t.getId()+"]","▶ START-"+t.getId()));
        }

        /* in progress */
        if(!prog.isEmpty()){
            rows.add(titleRow("==⏳ EN PROGRESO ⏳=="));
            for(Tasks t:prog){
                KeyboardRow r=new KeyboardRow();
                r.add(t.getDescription()+" [ID: "+t.getId()+"]");
                r.add("❌ CANCEL-"+t.getId());
                r.add("✅ DONE-"+t.getId());
                rows.add(r);
            }
        }

        /* done */
        if(!done.isEmpty()){
            rows.add(titleRow("==✅ COMPLETADAS ✅=="));
            for(Tasks t:done) rows.add(row(t.getDescription()+" [ID: "+t.getId()+"]","↩ UNDO-"+t.getId()));
        }

        kb.setKeyboard(rows);
        send(chatId,"Tablero Sprint "+sprintId,kb);
    }

    private List<Tasks> filterByStatus(List<Tasks> list,String st){
        List<Tasks> out=new ArrayList<Tasks>();
        for(Tasks t:list) if(st.equalsIgnoreCase(t.getStatus())) out.add(t);
        return out;
    }

    private void showAllTasks(long chatId,int sprintId){
        List<TaskAssignees> assigned = taskSvc.getTaskAssigneesBySprint(sprintId);
        List<SimplifiedTaskDTO> unassigned = taskSvc.getUnassignedTasksBySprint(sprintId);

        StringBuilder sb=new StringBuilder("*Sprint ").append(sprintId).append("*\n\n");
        sb.append("📥 *Asignadas*\n");
        if(assigned.isEmpty()) sb.append("— vacías —\n");
        else for(TaskAssignees ta:assigned)
            sb.append("• ").append(ta.getTask().getName())
              .append(" (").append(ta.getProjectUser().getUser().getName()).append(")\n");

        sb.append("\n📭 *Libres*\n");
        if(unassigned.isEmpty()) sb.append("— vacías —");
        else for(SimplifiedTaskDTO t:unassigned)
            sb.append("• ").append(t.getDescription())
              .append(" (SP ").append(t.getStoryPoints()).append(")\n");

        send(chatId,sb.toString(),true);
    }

    /* ========================================================= */
    /* ===============  ADMIN USERS  =========================== */
    /* ========================================================= */
    private void listOracleUsers(long chatId,ChatState st){
        st.oracleUsers=getAllOracleUsers();
        StringBuilder sb=new StringBuilder("*Usuarios disponibles:*\n\n");
        for(int i=0;i<st.oracleUsers.size();i++)
            sb.append(i+1).append(") ").append(st.oracleUsers.get(i).getName()).append("\n");
        sb.append("\nNúmero del usuario para agregar.");
        send(chatId,sb.toString(),true);
    }
    private void addUserFlow(long chatId,String txt,ChatState st){
        int idx;
        try{ idx=Integer.parseInt(txt.trim()); }catch(Exception e){ send(chatId,"Número",false);return; }
        if(idx<1||idx>st.oracleUsers.size()){ send(chatId,"Fuera de rango",false); return; }
        OracleUser u=st.oracleUsers.get(idx-1);

        HashMap<String,Object> payload=new HashMap<String,Object>();
        HashMap<String,Object> proj = new HashMap<String,Object>();
        proj.put("id_project",st.currentProjectId);
        payload.put("project",proj);
        payload.put("roleUser","developer");
        payload.put("status","active");
        HashMap<String,Object> user=new HashMap<String,Object>();
        user.put("idUser",u.getIdUser());
        payload.put("user",user);

        rest.postForEntity(baseUrl+"/api/project-users",payload,Object.class);
        send(chatId,"Usuario agregado.",false);
        showUsers(chatId,st.currentProjectId); reset(st);
    }

    private void toggleSprint(long chatId,int sprintId,String newStatus,ChatState st){
        HashMap<String,Object> p=new HashMap<String,Object>(); p.put("description",newStatus);
        rest.exchange(baseUrl+"/api/sprints/"+sprintId,HttpMethod.PATCH,new HttpEntity<HashMap<String,Object>>(p),Sprint.class);
        boolean mgr=roleSvc.isManagerInProject(st.currentProjectId, st.loggedUser.getIdUser());
        showSprintsForProject(chatId,st.currentProjectId,mgr);
    }
    private void showUsers(long chatId,int projectId){
        OracleUser[] arr=rest.getForObject(baseUrl+"/api/project-users/project/"+projectId+"/users",OracleUser[].class);
        StringBuilder sb=new StringBuilder("*Usuarios del proyecto:*\n\n");
        if(arr!=null&&arr.length>0) for(OracleUser u:arr) sb.append("• ").append(u.getName()).append("\n");
        else sb.append("Sin usuarios.");

        ReplyKeyboardMarkup kb=new ReplyKeyboardMarkup(); kb.setResizeKeyboard(true);
        List<KeyboardRow> rows=Arrays.asList(
                row("➕ Agregar Usuario"),
                row("⬅️ Volver a Sprints"));
        kb.setKeyboard(rows);
        send(chatId,sb.toString(),kb,true);
    }

    /* ========================================================= */
    /* ================== UTIL BACKEND & UI ==================== */
    /* ========================================================= */
    private OracleUser doLogin(String u,String p){
        LoginRequest req=new LoginRequest(); req.setName(u); req.setPassword(p);
        try{
            ResponseEntity<OracleUser> r=rest.postForEntity(baseUrl+"/users/login",req,OracleUser.class);
            return r.getStatusCode()==HttpStatus.OK? r.getBody():null;
        }catch(Exception e){ log.error("",e); return null; }
    }
    private List<OracleUser> getProjectUsers(int pid){
        OracleUser[] arr=rest.getForObject(baseUrl+"/api/project-users/project/"+pid+"/users",OracleUser[].class);
        return arr==null? new ArrayList<OracleUser>() : Arrays.asList(arr);
    }
    private List<OracleUser> getAllOracleUsers(){
        OracleUser[] arr=rest.getForObject(baseUrl+"/users",OracleUser[].class);
        return arr==null? new ArrayList<OracleUser>() : Arrays.asList(arr);
    }

    /* helpers UI */
    private void send(long chat,String txt,boolean md){ send(chat,txt,null,md); }
    private void send(long chat,String txt,ReplyKeyboardMarkup kb){ send(chat,txt,kb,false); }
    private void send(long chat,String txt,ReplyKeyboardMarkup kb,boolean md){
        SendMessage m=new SendMessage(String.valueOf(chat),txt);
        if(kb!=null) m.setReplyMarkup(kb);
        if(md) m.enableMarkdown(true);
        exec(m);
    }
    private void exec(SendMessage m){
        try{ execute(m);}catch(TelegramApiException e){ log.error("",e); }
    }
    private KeyboardRow row(String... texts){
        KeyboardRow r=new KeyboardRow();
        for(String t:texts) r.add(t);
        return r;
    }
    private KeyboardRow titleRow(String t){ return row(t); }

    private ChatState createState(long chatId){
        ChatState s=new ChatState(); chats.put(chatId,s); return s;
    }
    private void reset(ChatState st){ st.flow=Flow.NONE; st.step=0; }

    private int id(String t){ return Integer.parseInt(t.replaceAll("\\D","")); }
}
