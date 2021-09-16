import java.util.LinkedList;
import java.util.Scanner;

/**
 * Java控制台推箱子小游戏
 */
public class PushBox {

    // 程序入口
    public static void main(String[] args) {
        start();
    }

    /**
     * 开始游戏
     */
    private static void start() {
        Scanner sc = new Scanner(System.in);
        System.out.println("******欢迎使用java控制台推箱子小游戏******");
        int start_flag = init_op(sc);  // 取得关卡数
        if(start_flag == 0){
            print_exit();
            return;   // 终止游戏
        }
        // start_flag保存了关卡，开始加载关卡地图
        load_map(start_flag-1);  // 传递对应地图数组下标
        while(game_con){
            show_map_tip();
            int game_op = str_int(sc.next());  // 操作码
            switch (game_op){
                case UP:
                case LEFT:
                case RIGHT:
                case DOWN:{
                    move(game_op);
                    refresh_map();
                    check_over();
                    break;
                }
                case QUIT: {
                    game_con = false;
                    print_exit();
                    break;
                }
                case REPLAY:{
                    System.out.println("重玩关卡...");
                    load_map(now_gq);break;
                }
                case SELECT:{
                    select_gq(sc);break;
                }
                case CANCEL:{
                    cancel();
                    refresh_map();
                    break;
                }
                default:{
                    System.out.println("操作无效...");
                    refresh_map();
                    break;
                }
            }
        }
    }

    /**
     * 游戏初始化操作，选择游戏关卡，或退出。
     */
    private static int init_op(Scanner cin) {
        while (true){
            System.out.println("1：开始, 2：关于作者, 0：退出");
            int init_start = str_int(cin.next());
            if(init_start==1){
                int init_sgq = 0;
                while(init_sgq<=0 || init_sgq>maps.length){
                    System.out.println("请输入关卡，[1-"+maps.length+"]");
                    init_sgq = str_int(cin.next());
                }

                return init_sgq;  // 初始化关卡数
            }
            else if(init_start==0){
                break;
            }else if(init_start==2){
                System.out.println("Power By 朱丰华, 2021, 联系QQ: 1344694396");
            }
            else{
                System.out.println("输入有误, 请重新输入...");
            }
        }
        return 0;  // 返回0则说明直接退出了。
    }

    /**
     * 整数输入封装
     */
    private static int str_int(String str){
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i)>='0' && str.charAt(i)<='9'){
                return Integer.parseInt(str);
            }
        }
        return -1;
    }

    /**
     * 打印退出语句
     */
    private static void print_exit(){
        System.out.println("******游戏退出，欢迎下次使用******");
    }

    /**
     * 查找拉箱子时，背后的点，也就是箱子所在的点
     * @param dir 拉动方向
     */
    private static void find_back_point(int dir) {
        switch(dir){
            case UP:{
                back_point[0] = hero[0]+1;
                back_point[1] = hero[1];
                break;
            }
            case DOWN:{
                back_point[0] = hero[0]-1;
                back_point[1] = hero[1];
                break;
            }
            case LEFT:{
                back_point[0] = hero[0];
                back_point[1] = hero[1]+1;
                break;
            }
            case RIGHT:{
                back_point[0] = hero[0];
                back_point[1] = hero[1]-1;
                break;
            }
        }
    }

    /**
     * 关卡选择
     */
    private static void select_gq(Scanner sc) {
        int select_gq = 0;
        while (select_gq<=0 || select_gq>maps.length){
            System.out.println("关卡范围[1,"+maps.length+"], 请选择：");
            select_gq = str_int(sc.next());
        }
        load_map(select_gq-1);
    }

    /**
     * 加载地图
     * @param start_gq 关卡数
     */
    private static void load_map(int start_gq) {
        now_gq = start_gq;  // 记录当前关卡地图下标
        if(now_gq<0 || now_gq>=maps.length){
            System.out.println("地图已全部通关..."); // 关卡不合法
            print_exit();
            game_con = false;  // 终止游戏
            return;
        }
        System.out.println("当前关卡"+(start_gq+1)+",正在加载地图...");
        traps = 0;
        for (int i = 0; i< maps[start_gq].length; i++){
            for (int j = 0; j < maps[start_gq][i].length; j++) {
                System.out.print(map_chars[maps[start_gq][i][j]]+" ");
                // 把地图信息传给空白地图
                map_t[i][j] = maps[start_gq][i][j];
                // 统计地图陷阱数
                if(map_t[i][j] == TRAP || map_t[i][j] == GOOD ||  map_t[i][j] == IN){
                    traps++;
                }
            }
            System.out.println();
        }
        // 步骤记录清空
        steps.clear();
    }

    /**
     * 地图提示
     */
    private static void show_map_tip() {
        System.out.println(map_chars[1]+"：围墙   "+map_chars[2]+"：陷阱   "
                +map_chars[3]+"：箱子    "+map_chars[4]+"：人物   "
                +map_chars[5]+"：箱子填入陷阱    "+map_chars[6]+"：小人在陷阱上方");
        System.out.println(UP+":上, "+DOWN+":下, "+LEFT+":左, "+RIGHT+":右, "
                +CANCEL+"退步, "+REPLAY+":重玩, "+SELECT+":选关(当前"+(now_gq+1)+"), "+QUIT+":退出"
        );
    }

    /**
     * 更新地图
     */
    private static void refresh_map() {
        for (int[] ints : map_t) {
            for (int anInt : ints) {
                System.out.print(map_chars[anInt] + " ");
            }
            System.out.println();
        }
    }


    /**
     * 找到小人当前定位，并赋值到hero数组中
     */
    private static void find_hero() {
        for (int i = 0; i < map_t.length; i++) {
            for (int j = 0; j < map_t[i].length; j++) {
                if(map_t[i][j] == MAN){
                    hero[0] = i;
                    hero[1] = j;
                    hero[2] = 0;
                }else if(map_t[i][j] == IN){
                    hero[0] = i;
                    hero[1] = j;
                    hero[2] = 1;
                }
            }
        }
    }

    /**
     * 查找指定方向的后1，2点位置
     */
    private static void find_next(int dir) {
        switch (dir){
            case LEFT:{
                next_point[0] = hero[0];
                next_point[1] = hero[1]-1;

                next2_point[0] = hero[0];
                next2_point[1] = hero[1]-2;
                break;
            }
            case RIGHT:{
                next_point[0] = hero[0];
                next_point[1] = hero[1]+1;

                next2_point[0] = hero[0];
                next2_point[1] = hero[1]+2;
                break;
            }
            case UP:{
                next_point[0] = hero[0]-1;
                next_point[1] = hero[1];

                next2_point[0] = hero[0]-2;
                next2_point[1] = hero[1];
                break;
            }
            case DOWN:{
                next_point[0] = hero[0]+1;
                next_point[1] = hero[1];

                next2_point[0] = hero[0]+2;
                next2_point[1] = hero[1];
                break;
            }
        }
    }

    /**
     * 角色移动基本逻辑处理
     * @param dir  移动方向
     */
    private static void move(int dir) {
        System.out.println("正在移动人物...");
        steps.add(dir);  // 存下步骤，用于回退，但需要过滤无效步骤
        find_hero();
        find_next(dir);
        // 简单分为不移动，和可移动
        // 不可移动，均为block状态

        // 0w, 人墙
        if(map_t[next_point[0]][next_point[1]] == WALL){
            mv_actions("block");
        }

        // 人箱墙
        // 0gw
        else if(map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == WALL){
            mv_actions("block");
        }
        // 0bw
        else if(map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == WALL){
            mv_actions("block");
        }
        // 人箱箱
        // 0bb
        else if(map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == BOX){
            mv_actions("block");
        }
        // 0bg
        else if(map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == GOOD){
            mv_actions("block");
        }
        // 0gb
        else if(map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == BOX){
            mv_actions("block");
        }
        // 0gg
        else if(map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == GOOD){
            mv_actions("block");
        }

        // 下面均为可移动情况
        // 人白
        // ms
        else if(hero[2] == 0 && map_t[next_point[0]][next_point[1]] == SPACE){
            mv_actions("ms");
        }
        // mt
        else if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==TRAP){
            mv_actions("mt");
        }
        // is
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==SPACE ){
            mv_actions("is");
        }
        // it
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==TRAP ){
            mv_actions("it");
        }

        // 人箱白, 只有这里推动了箱子
        // mbs
        else if(hero[2]==0 && map_t[next_point[0]][next_point[1]] == BOX && map_t[next2_point[0]][next2_point[1]] == SPACE){
            mv_actions("mbs");
        }
        // mbt
        else if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == TRAP){
            mv_actions("mbt");
        }
        // mgs
        else if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == SPACE){
            mv_actions("mgs");
        }
        // mgt
        else if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == TRAP){
            mv_actions("mgt");
        }
        // ibs
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == SPACE){
            mv_actions("ibs");
        }
        // ibt
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==BOX && map_t[next2_point[0]][next2_point[1]] == TRAP){
            mv_actions("ibt");
        }
        // igs
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == SPACE){
            mv_actions("igs");
        }
        // igt
        else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==GOOD && map_t[next2_point[0]][next2_point[1]] == TRAP){
            mv_actions("igt");
        }
    }

    /**
     * 针对各种移动情况进行具体处理
     * @param action  移动情况描述
     */
    private static void mv_actions(String action) {
        switch (action){
            case "block":{
                System.out.println("移动无效...");
                steps.removeLast();  // 不保留历史记录
                break;
            }
            // 人白, 4种
            case "ms" : {
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(false);
                break;
            }
            case "mt":{
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(false);
                break;
            }
            case "is":{
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(false);
                break;
            }
            case "it":{
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(false);
                break;
            }
            // 人墙白, 8种, pushes
            case "mbs":{
                map_t[next2_point[0]][next2_point[1]] = BOX;
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(true);
                break;
            }
            case "mbt":{
                map_t[next2_point[0]][next2_point[1]] = GOOD;
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(true);
                break;
            }
            case "mgs":{
                map_t[next2_point[0]][next2_point[1]] = BOX;
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(true);
                break;
            }
            case "mgt":{
                map_t[next2_point[0]][next2_point[1]] = GOOD;
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = SPACE;
                pushes.add(true);
                break;
            }
            case "ibs":{
                map_t[next2_point[0]][next2_point[1]] = BOX;
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(true);
                break;
            }
            case "ibt":{
                map_t[next2_point[0]][next2_point[1]] = GOOD;
                map_t[next_point[0]][next_point[1]] = MAN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(true);
                break;
            }
            case "igs":{
                map_t[next2_point[0]][next2_point[1]] = BOX;
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(true);
                break;
            }
            case "igt":{
                map_t[next2_point[0]][next2_point[1]] = GOOD;
                map_t[next_point[0]][next_point[1]] = IN;
                map_t[hero[0]][hero[1]] = TRAP;
                pushes.add(true);
                break;
            }
        }
    }


    /**
     * 回退一步基本逻辑
     */
    private static void cancel() {
        int dir;
        if(steps.size()==0){
            System.out.println("回退无效...");
            return;  // 没有历史步骤
        }
        dir = steps.removeLast();  // 存在步骤, 取出最后一步, 反向拉
        switch (dir){
            case UP:{
                pull(DOWN);
                break;
            }
            case DOWN:{
                pull(UP);
                break;
            }
            case LEFT:{
                pull(RIGHT);
                break;
            }
            case RIGHT:{
                pull(LEFT);
                break;
            }
        }
    }

    /**
     * 拉箱子进一步处理
     * @param dir 拉动的方向
     */
    private static void pull(int dir) {
        find_hero();
        find_next(dir);
        find_back_point(dir);
        // 因为是反向操作，且只保留了有效推箱子，故必定可以拉动
        // 并不能简单反向拉，而必须先分析背后是否有箱子
        System.out.println("回退了一步...");

        // 应该分为已推动8种，未推动4种
        Boolean pushed = pushes.removeLast(); // 当前步骤是否曾推动, true是"人墙白"8种, 否则是"人白"4种

        // 未推动
        if(!pushed){
            // ms, 对应顺序是 hero, next, 其中hero有2种状态, next必定为空白
            if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[next_point[0]][next_point[1]]=MAN;
                map_t[hero[0]][hero[1]] = SPACE;
            }
            // mt
            else if(hero[2]==0 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[next_point[0]][next_point[1]]=IN;
                map_t[hero[0]][hero[1]] = SPACE;
            }
            // is
            else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[next_point[0]][next_point[1]]=MAN;
                map_t[hero[0]][hero[1]] = TRAP;
            }
            // it
            else if(hero[2]==1 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[next_point[0]][next_point[1]]=IN;
                map_t[hero[0]][hero[1]] = TRAP;
            }
        }
        // 已推动, 8种
        else{
            // bms, 对应顺序是back, hero, next, 其中 back 必为箱, hero 有2种, next 必定为空白
            if(map_t[back_point[0]][back_point[1]]==BOX && hero[2]==0 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[back_point[0]][back_point[1]] = SPACE;
                map_t[hero[0]][hero[1]] = BOX;
                map_t[next_point[0]][next_point[1]]=MAN;
            }
            // gms
            else if(map_t[back_point[0]][back_point[1]]==GOOD && hero[2]==0 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[back_point[0]][back_point[1]] = TRAP;
                map_t[hero[0]][hero[1]] = BOX;
                map_t[next_point[0]][next_point[1]]=MAN;
            }
            // bis
            else if(map_t[back_point[0]][back_point[1]]==BOX && hero[2]==1 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[back_point[0]][back_point[1]] = SPACE;
                map_t[hero[0]][hero[1]] = GOOD;
                map_t[next_point[0]][next_point[1]]=MAN;
            }
            // gis
            else if(map_t[back_point[0]][back_point[1]]==GOOD && hero[2]==1 && map_t[next_point[0]][next_point[1]]==SPACE){
                map_t[back_point[0]][back_point[1]] = TRAP;
                map_t[hero[0]][hero[1]] = GOOD;
                map_t[next_point[0]][next_point[1]]=MAN;
            }
            // bmt
            else if(map_t[back_point[0]][back_point[1]]==BOX && hero[2]==0 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[back_point[0]][back_point[1]] = SPACE;
                map_t[hero[0]][hero[1]] = BOX;
                map_t[next_point[0]][next_point[1]]=IN;
            }
            // gmt
            else if(map_t[back_point[0]][back_point[1]]==GOOD && hero[2]==0 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[back_point[0]][back_point[1]] = TRAP;
                map_t[hero[0]][hero[1]] = BOX;
                map_t[next_point[0]][next_point[1]]=IN;
            }
            // bit
            else if(map_t[back_point[0]][back_point[1]]==BOX && hero[2]==1 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[back_point[0]][back_point[1]] = SPACE;
                map_t[hero[0]][hero[1]] = GOOD;
                map_t[next_point[0]][next_point[1]]=IN;
            }

            // git
            else if(map_t[back_point[0]][back_point[1]]==GOOD && hero[2]==1 && map_t[next_point[0]][next_point[1]]==TRAP){
                map_t[back_point[0]][back_point[1]] = TRAP;
                map_t[hero[0]][hero[1]] = GOOD;
                map_t[next_point[0]][next_point[1]]=IN;
            }
        }

    }

    /**
     * 检测关卡是否结束
     */
    private static void check_over() {
        goods = 0;
        for (int[] ints : map_t) {
            for (int anInt : ints) {
                if (anInt == GOOD) {
                    goods++;
                }
            }
        }
        if(goods == traps){
            System.out.println("当前关卡通关，自动进入下一关...");
            load_map(now_gq+1);
        }
    }

    /**
     * 游戏控制
     */
    static boolean game_con = true;
    static int now_gq = 0;  // 当前关卡数
    static final int CANCEL = 33;  // 回退
    static final int REPLAY = 99;  // 重玩
    static final int SELECT = 77;  // 选关
    static final int QUIT = 55;  // 退出

    /**
     * 移动数字识别
     */
    static final int UP = 8;   // 向上
    static final int DOWN = 2;  // 向下
    static final int LEFT = 4;  // 向左
    static final int RIGHT = 6;  // 向右

    /**
     * 地图符号识别
     *
     * 空白：①SPACE，②TRAP
     * 墙：①WALL
     * 箱子：①BOX，②GOOD
     * 人物：①MAN，②IN
     */
    static final int SPACE = 0;  // 空白占位符
    static final int WALL = 1;  // 墙
    static final int TRAP = 2;  // 陷阱
    static final int BOX = 3;  // 箱子
    static final int MAN = 4;  // 人物
    static final int GOOD = 5;  // 箱子填入陷阱
    static final int IN = 6;  // 小人在陷阱上方

    /**
     * 地图显示符号组，对应识别符号
     */
    static char[] map_chars = {' ','■','◌','●','☥','◎','*'};

    /**
     * 当前关卡地图
     */
    static int[][] map_t = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    /**
     * 人物所在地图的坐标点，以及下1，2个点的坐标点
     */
    static int[] hero = {0,0,0};  // 人物坐标定位，(x,y)，以及小人脚下是否有陷阱
    static int[] next_point = {0,0};  // 定向下一个点
    static int[] next2_point = {0,0};  // 定向下两个点的坐标，如果第一点是墙，此点可能越界

    /**
     * 陷阱统计
     */
    static int traps = 0;  // 当前地图总陷阱数
    static int goods = 0;  // 当前地图已填陷阱

    /**
     * 记录历史步数, 等操作, 用于回退
     */
    // 记录有效步骤方向
    static LinkedList<Integer> steps = new LinkedList<>();  // block状态不保存步骤, 其他移动需要记录
    // 记录该步骤是否曾推动箱子, 若是则拉箱子, 否则不拉
    static LinkedList<Boolean> pushes = new LinkedList<>();  // true对应"人箱白", false对应"人白"
    static int[] back_point = {0,0};  // 拉箱子时, 箱子在背后, 箱子所在的点
    /**
     * 从 map1 到 n，是每个关卡地图
     */
    static int[][] map1 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 3, 0, 3, 2, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 0, 3, 4, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 3, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map2 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 3, 1, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 0, 1, 0, 1, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map3 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 3, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 4, 0, 3, 0, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 2, 2, 1, 0, 3, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 2, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map4 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 4, 3, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 3, 0, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 5, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map5 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 4, 0, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 3, 0, 0, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 0, 0, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map6 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 2, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 3, 0, 3, 1, 2, 0, 1, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 0, 5, 0, 0, 1, 0, 1, 0, 0, 0, 0},
            {0, 1, 0, 2, 1, 3, 0, 3, 0, 1, 0, 1, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0},
            {0, 0, 1, 0, 1, 1, 1, 2, 0, 0, 0, 0, 4, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map7 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 3, 0, 3, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 3, 1, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 3, 0, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 2, 2, 2, 2, 2, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map8 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 2, 0, 3, 1, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 2, 2, 3, 0, 3, 0, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 1, 2, 2, 0, 3, 0, 3, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map9 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 3, 0, 1, 1, 1, 0, 3, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 1, 2, 2, 2, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 1, 2, 2, 2, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 0, 0, 3, 0, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 4, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map10 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 3, 3, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 4, 0, 3, 2, 2, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 2, 2, 2, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map11 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 1, 1, 1, 1, 3, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 3, 2, 2, 2, 2, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 4, 0, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map12 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 0, 4, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 2, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 2, 3, 2, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 5, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map13 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 2, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 2, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 3, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 3, 0, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 3, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map14= {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 2, 2, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 4, 3, 2, 5, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 2, 2, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map15= {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 0, 3, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 4, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map16 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 3, 0, 1, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 2, 2, 2, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 2, 2, 2, 3, 1, 3, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 0, 1, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map17 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 3, 3, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 2, 2, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 2, 2, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 4, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map18 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 3, 2, 2, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 0, 1, 5, 2, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 1, 1, 3, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 4, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map19 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 3, 1, 3, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 3, 0, 0, 1, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 4, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map20 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 3, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 1, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 3, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 3, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 4, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map21 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 2, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 1, 1, 3, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 0, 3, 3, 0, 1, 0, 0, 0},
            {0, 0, 1, 4, 0, 3, 0, 3, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map22 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 1, 3, 3, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 2, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 2, 2, 2, 3, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 3, 0, 0, 3, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map23 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0, 3, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 0, 3, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map24 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0},
            {0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0, 2, 1, 0},
            {0, 1, 0, 0, 3, 0, 3, 0, 1, 0, 1, 2, 2, 2, 1, 0},
            {0, 1, 0, 1, 0, 0, 3, 0, 1, 1, 1, 0, 0, 2, 1, 0},
            {0, 1, 0, 0, 3, 3, 3, 0, 0, 0, 3, 0, 4, 2, 1, 0},
            {0, 1, 1, 1, 0, 0, 3, 0, 0, 3, 1, 0, 0, 2, 1, 0},
            {0, 0, 0, 1, 0, 0, 3, 1, 3, 0, 1, 2, 2, 2, 1, 0},
            {0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map25 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 2, 2, 1, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 3, 2, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 0, 2, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 1, 1, 3, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 1, 3, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 4, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map26 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 3, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 0, 5, 0, 1, 0, 1, 1, 0},
            {0, 0, 0, 1, 0, 3, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0},
            {0, 0, 1, 1, 0, 1, 1, 1, 0, 2, 0, 1, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 3, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 3, 4, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 3, 1, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map27 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 3, 1, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0},
            {1, 2, 2, 1, 0, 0, 1, 1, 0, 3, 0, 3, 1, 0, 1, 0},
            {1, 2, 2, 0, 0, 0, 1, 1, 0, 3, 0, 3, 0, 0, 1, 0},
            {1, 2, 2, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0},
            {1, 2, 2, 1, 0, 1, 0, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {1, 2, 2, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 0, 4, 0, 1, 1, 1, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map28 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 3, 0, 3, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 3, 0, 0, 1, 0, 1, 5, 2, 5, 2, 5, 1},
            {1, 1, 0, 3, 0, 3, 0, 1, 1, 1, 2, 5, 2, 5, 2, 1},
            {0, 1, 3, 0, 3, 0, 0, 1, 0, 0, 5, 2, 5, 2, 5, 1},
            {0, 1, 4, 3, 0, 3, 0, 0, 0, 0, 2, 5, 2, 5, 1, 1},
            {0, 1, 3, 0, 3, 0, 0, 1, 0, 0, 5, 2, 5, 2, 5, 1},
            {1, 1, 0, 3, 0, 3, 0, 1, 1, 1, 2, 5, 2, 5, 2, 1},
            {1, 0, 0, 0, 3, 0, 0, 1, 0, 1, 5, 2, 5, 2, 5, 1},
            {1, 0, 0, 3, 0, 3, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map29 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 0, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 0, 1, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 3, 0, 3, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map30 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 3, 1, 1, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 4, 3, 2, 0, 2, 0, 2, 3, 1, 1, 0, 0},
            {0, 0, 1, 1, 0, 3, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map31 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 2, 0, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 3, 3, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 1, 1, 2, 1, 1, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 3, 2, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map32 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 0, 2, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 0, 3, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 3, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map33 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 3, 0, 0, 0, 1, 1, 1, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 3, 3, 0, 1, 0},
            {0, 0, 0, 1, 0, 3, 3, 0, 1, 3, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 0, 0, 0, 3, 0, 0, 0, 3, 0, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 1, 1, 1, 0},
            {0, 1, 2, 2, 4, 0, 1, 3, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 1, 2, 1, 2, 2, 0, 0, 3, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 2, 2, 2, 2, 3, 1, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 2, 2, 2, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map34 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {1, 0, 3, 0, 1, 3, 0, 1, 0, 3, 1, 1, 3, 0, 1, 0},
            {1, 0, 1, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {1, 0, 0, 0, 1, 1, 3, 1, 3, 1, 1, 3, 3, 0, 1, 0},
            {1, 0, 1, 0, 1, 0, 2, 2, 2, 0, 1, 0, 0, 0, 1, 0},
            {1, 0, 3, 0, 0, 2, 0, 1, 0, 2, 3, 0, 1, 0, 1, 0},
            {1, 0, 3, 1, 4, 3, 2, 2, 2, 1, 0, 1, 0, 0, 1, 0},
            {1, 0, 0, 0, 0, 2, 0, 1, 0, 2, 0, 0, 3, 0, 1, 0},
            {1, 0, 1, 1, 2, 3, 1, 1, 1, 3, 2, 0, 1, 0, 1, 0},
            {1, 0, 1, 0, 3, 2, 2, 2, 2, 2, 0, 1, 1, 0, 1, 0},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map35 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 1, 0, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 5, 2, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 1, 2, 4, 2, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 3, 1, 1, 1, 5, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map36 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 3, 3, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 3, 0, 3, 3, 3, 0, 1, 1, 1, 1, 1, 0, 0},
            {1, 1, 0, 1, 1, 0, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0},
            {0, 1, 0, 1, 4, 1, 2, 2, 2, 1, 1, 1, 3, 0, 1, 0},
            {0, 1, 0, 1, 0, 3, 2, 2, 2, 0, 0, 0, 0, 0, 1, 0},
            {1, 1, 0, 1, 0, 3, 2, 2, 2, 3, 0, 1, 0, 1, 1, 0},
            {1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0},
            {1, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map37 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 4, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 3, 3, 3, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 2, 1, 3, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 2, 3, 2, 0, 2, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 2, 1, 2, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map38 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 2, 2, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0},
            {0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
            {0, 0, 1, 2, 2, 0, 0, 1, 0, 3, 1, 1, 0, 1, 0, 0},
            {0, 0, 1, 2, 2, 2, 0, 1, 3, 0, 3, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 3, 3, 0, 1, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 3, 0, 3, 3, 0, 0, 1, 0, 0},
            {0, 0, 0, 1, 4, 0, 3, 3, 3, 0, 0, 1, 0, 1, 0, 0},
            {0, 0, 0, 1, 1, 0, 3, 0, 1, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map39 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 0, 2, 2, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 2, 2, 0, 2, 2, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 3, 0, 3, 0, 3, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 4, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map40 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0},
            {1, 4, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0},
            {1, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0},
            {0, 1, 0, 0, 3, 0, 0, 0, 1, 1, 1, 1, 3, 1, 1, 0},
            {0, 1, 3, 0, 1, 1, 0, 1, 0, 3, 0, 3, 0, 1, 0, 0},
            {1, 1, 0, 3, 0, 0, 3, 1, 0, 0, 0, 0, 0, 1, 0, 0},
            {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0},
            {1, 0, 0, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 0, 0},
            {1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 0, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    /**
     * maps 表示所有地图对象
     * 由于 jvm 限制单个方法最大64k, 引用多个类的静态方法, 一个类存 40 个地图
     */
    static int[][][] maps = {
            map1,map2,map3,map4,map5,map6,map7,map8,map9,map10,
            map11,map12,map13,map14,map15,map16,map17,map18,map19,map20,
            map21,map22,map23,map24,map25,map26,map27,map28,map29,map30,
            map31,map32,map33,map34,map35,map36,map37,map38,map39,map40
            ,Maps1.map41,Maps1.map42,Maps1.map43,Maps1.map44,Maps1.map45,Maps1.map46,Maps1.map47,Maps1.map48,Maps1.map49,Maps1.map50
            ,Maps1.map51,Maps1.map52,Maps1.map53,Maps1.map54,Maps1.map55,Maps1.map56,Maps1.map57,Maps1.map58,Maps1.map59,Maps1.map60
            ,Maps1.map61,Maps1.map62,Maps1.map63,Maps1.map64,Maps1.map65,Maps1.map66,Maps1.map67,Maps1.map68,Maps1.map69,Maps1.map70
            ,Maps1.map71,Maps1.map72,Maps1.map73,Maps1.map74,Maps1.map75,Maps1.map76,Maps1.map77,Maps1.map78,Maps1.map79,Maps1.map80
            ,Maps2.map81,Maps2.map82,Maps2.map83,Maps2.map84,Maps2.map85,Maps2.map86,Maps2.map87,Maps2.map88,Maps2.map89,Maps2.map90
            ,Maps2.map91,Maps2.map92,Maps2.map93,Maps2.map94,Maps2.map95,Maps2.map96,Maps2.map97,Maps2.map98,Maps2.map99,Maps2.map100
    };
}
class Maps1{ // 每个 class 存放40个地图, Maps1对应41-80
    static int[][] map41 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 1, 2, 5, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 5, 2, 1, 3, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 4, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map42 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0},
            {0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 3, 0, 4, 0, 1, 0},
            {0, 0, 1, 1, 0, 1, 3, 1, 1, 2, 1, 1, 0, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 2, 2, 2, 5, 2, 0, 3, 0, 1, 0},
            {0, 0, 0, 1, 0, 3, 1, 0, 1, 2, 1, 0, 1, 0, 1, 0},
            {0, 0, 0, 1, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map43 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 3, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 0, 3, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 2, 0, 3, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 2, 1, 0, 0, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0},
            {1, 0, 3, 0, 3, 0, 0, 0, 3, 0, 0, 1, 0, 0, 1, 0},
            {1, 0, 0, 0, 0, 3, 3, 0, 0, 0, 3, 0, 3, 0, 1, 0},
            {1, 1, 1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0, 0, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map44 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 0, 4, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 2, 1, 2, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 1, 2, 1, 5, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 2, 2, 2, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 3, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map45 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 3, 3, 0, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 0, 1, 0, 0, 1, 0},
            {1, 0, 0, 1, 2, 0, 2, 2, 0, 1, 1, 1, 3, 1, 1, 0},
            {1, 0, 0, 1, 2, 1, 5, 2, 3, 0, 0, 0, 0, 0, 1, 0},
            {1, 0, 0, 1, 2, 1, 2, 5, 1, 0, 1, 0, 0, 0, 1, 0},
            {1, 0, 3, 3, 2, 2, 2, 2, 1, 0, 1, 1, 1, 1, 1, 0},
            {1, 0, 4, 3, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 3, 3, 3, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map46 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 3, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 3, 0, 3, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 1, 1, 0, 0, 0, 0},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {1, 0, 3, 0, 3, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0},
            {1, 0, 3, 0, 0, 1, 1, 0, 3, 3, 3, 0, 1, 0, 0, 0},
            {1, 4, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0},
            {1, 1, 0, 1, 0, 1, 2, 3, 3, 3, 3, 3, 2, 1, 0, 0},
            {0, 1, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 0},
            {0, 1, 0, 0, 0, 2, 5, 5, 5, 5, 5, 5, 5, 2, 1, 0},
            {0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map47 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 4, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 3, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 2, 1, 1, 3, 1, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 3, 1, 2, 2, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 0, 2, 2, 2, 1, 1, 3, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 1, 2, 1, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 3, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map48 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 4, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 3, 2, 3, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 2, 3, 2, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 2, 3, 2, 3, 2, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 3, 2, 3, 2, 3, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map49 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 5, 2, 2, 1, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 5, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 5, 2, 2, 1, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 5, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 5, 2, 2, 1, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 5, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 5, 2, 2, 1, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 5, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 5, 2, 2, 1, 0},
            {0, 0, 1, 4, 3, 0, 3, 0, 3, 0, 5, 2, 2, 2, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map50 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 3, 0, 0, 0, 1, 0},
            {0, 1, 1, 0, 1, 2, 1, 2, 1, 2, 1, 4, 3, 1, 0, 0},
            {1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 1, 0},
            {0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 3, 1, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 3, 0, 3, 0, 3, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map51 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 2, 3, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 2, 3, 2, 3, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 5, 3, 2, 3, 2, 4, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 2, 3, 2, 3, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 2, 3, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map52 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 5, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1},
            {1, 0, 3, 3, 3, 3, 5, 5, 5, 5, 3, 2, 2, 2, 4, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 5, 0, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map53 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 4, 3, 0, 0, 0, 0, 3, 0, 0, 3, 0, 1, 0},
            {0, 0, 1, 3, 1, 1, 1, 0, 3, 0, 1, 0, 1, 0, 1, 0},
            {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 3, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 3, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0, 3, 0, 1, 3, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map54 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 3, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 1, 1, 0, 5, 0, 1, 0, 1, 1, 0, 0},
            {0, 0, 1, 0, 3, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0},
            {0, 1, 1, 0, 1, 1, 1, 0, 2, 0, 1, 0, 1, 0, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 3, 1, 1, 1, 3, 1, 0, 0, 0},
            {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 3, 4, 1, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 3, 1, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map55 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 3, 4, 3, 3, 3, 3, 3, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map56 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 4, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 2, 0, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 5, 5, 5, 5, 5, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 3, 5, 3, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 5, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map57 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 1, 2, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 0, 1, 0},
            {0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0},
            {0, 1, 2, 1, 2, 5, 0, 3, 0, 2, 2, 3, 5, 1, 1, 0},
            {0, 1, 2, 1, 0, 3, 0, 3, 0, 5, 2, 3, 4, 1, 0, 0},
            {0, 1, 2, 1, 2, 0, 0, 3, 0, 2, 2, 3, 3, 1, 0, 0},
            {0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0, 0},
            {0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 1, 2, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 0, 0},
            {0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map58 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 1, 2, 2, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0},
            {0, 1, 2, 2, 0, 0, 1, 0, 3, 0, 0, 3, 0, 0, 1, 0},
            {0, 1, 2, 2, 0, 0, 1, 3, 1, 1, 1, 1, 0, 0, 1, 0},
            {0, 1, 2, 2, 0, 0, 0, 0, 4, 0, 1, 1, 0, 0, 1, 0},
            {0, 1, 2, 2, 0, 0, 1, 0, 1, 0, 0, 3, 0, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 0, 3, 0, 1, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 3, 0, 3, 0, 3, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map59 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0},
            {0, 1, 0, 0, 0, 1, 1, 2, 2, 2, 2, 2, 0, 0, 1, 0},
            {0, 1, 0, 0, 3, 0, 0, 1, 1, 2, 2, 2, 1, 0, 1, 0},
            {0, 1, 1, 0, 0, 3, 0, 0, 1, 1, 1, 0, 1, 0, 1, 0},
            {0, 0, 1, 0, 1, 0, 3, 0, 0, 1, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 1, 0, 3, 0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 3, 0, 0, 1, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 0, 1, 0, 3, 0, 1, 0, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 3, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 3, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 4, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map60 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0},
            {0, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 0},
            {0, 1, 5, 2, 5, 2, 2, 2, 2, 2, 5, 2, 5, 2, 1, 0},
            {0, 1, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 1, 0},
            {0, 1, 3, 0, 3, 0, 3, 4, 3, 0, 3, 0, 3, 0, 1, 0},
            {0, 1, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 1, 0},
            {0, 1, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 1, 0},
            {0, 1, 2, 5, 2, 5, 2, 2, 2, 2, 2, 5, 2, 5, 1, 0},
            {0, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 0},
            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map61 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 2, 5, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 0, 1, 2, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 2, 5, 2, 1, 4, 1, 1, 0, 0},
            {0, 0, 1, 0, 1, 0, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 3, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map62 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 5, 2, 2, 5, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 5, 2, 2, 5, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 4, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map63 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 2, 0, 0, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 2, 3, 2, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 4, 3, 1, 0, 1, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 2, 0, 2, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 3, 1, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 2, 0, 2, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map64 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 2, 2, 2, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 0, 3, 3, 3, 5, 5, 5, 0, 3, 4, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 2, 2, 2, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map65 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 3, 1, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 3, 0, 0, 2, 3, 2, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 4, 1, 1, 1, 2, 2, 2, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map66 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 3, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 3, 4, 3, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 2, 2, 2, 3, 0, 3, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 2, 2, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map67 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {1, 2, 2, 2, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {1, 2, 2, 2, 1, 1, 1, 0, 0, 3, 0, 0, 1, 0, 0, 0},
            {1, 2, 2, 2, 2, 1, 1, 0, 3, 0, 0, 3, 1, 1, 1, 0},
            {1, 1, 2, 2, 2, 2, 1, 1, 0, 0, 0, 3, 0, 0, 1, 0},
            {1, 1, 1, 2, 2, 2, 0, 1, 1, 0, 3, 0, 3, 0, 1, 0},
            {1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 1, 0},
            {1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1},
            {1, 0, 3, 0, 1, 0, 1, 3, 0, 0, 3, 0, 0, 0, 0, 1},
            {1, 0, 0, 3, 0, 4, 0, 3, 0, 0, 0, 0, 3, 0, 0, 1},
            {1, 0, 0, 0, 1, 0, 3, 0, 3, 3, 0, 3, 0, 1, 1, 1},
            {1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0},
            {1, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map68 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 2, 0, 2, 0, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 3, 3, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 2, 3, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 3, 3, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 2, 0, 2, 0, 2, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map69 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 3, 1, 1, 4, 3, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 3, 1, 2, 2, 2, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 2, 2, 2, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 1, 2, 0, 2, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 3, 0, 0, 3, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][]  map70 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 5, 1, 1, 1, 0, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 2, 2, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 1, 0, 1, 5, 1, 1, 1, 3, 1, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 4, 0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map71 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 2, 2, 2, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {1, 2, 2, 2, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 2, 2, 2, 2, 0, 0, 0, 3, 3, 1, 1, 1, 1, 1, 0},
            {1, 2, 2, 2, 2, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1},
            {1, 2, 2, 1, 3, 1, 1, 1, 1, 0, 1, 3, 1, 0, 0, 1},
            {1, 1, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 3, 3, 0, 1},
            {1, 0, 0, 3, 1, 0, 4, 0, 3, 0, 3, 3, 1, 0, 0, 1},
            {1, 0, 3, 0, 3, 0, 3, 0, 1, 0, 0, 0, 3, 0, 1, 1},
            {1, 0, 0, 0, 1, 0, 0, 3, 0, 1, 1, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map72 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 2, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0},
            {1, 1, 2, 2, 1, 0, 3, 0, 0, 3, 0, 1, 0, 0, 1, 0},
            {1, 2, 2, 2, 1, 0, 1, 1, 0, 3, 0, 1, 0, 0, 1, 0},
            {1, 2, 2, 2, 2, 2, 0, 0, 1, 3, 3, 0, 0, 0, 1, 0},
            {1, 1, 2, 2, 2, 2, 3, 0, 0, 0, 0, 1, 3, 0, 1, 0},
            {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0},
            {1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
            {1, 0, 0, 3, 0, 1, 0, 0, 3, 1, 0, 3, 0, 1, 1, 0},
            {1, 0, 3, 1, 1, 1, 0, 3, 0, 1, 0, 3, 3, 0, 1, 0},
            {1, 0, 0, 0, 4, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map73 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 0, 3, 3, 2, 2, 2, 1, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 1, 0},
            {0, 1, 0, 1, 1, 1, 1, 1, 1, 3, 1, 2, 2, 2, 1, 0},
            {1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 1, 0},
            {1, 0, 0, 1, 0, 1, 3, 0, 3, 0, 1, 1, 1, 1, 1, 0},
            {1, 0, 1, 0, 3, 0, 3, 0, 3, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 4, 0, 0, 3, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 3, 0, 3, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map74 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 0, 0, 0, 0, 2, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 0, 1, 3, 2, 1, 3, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 1, 0, 0, 4, 2, 1, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 0, 1, 1, 1, 1, 2, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 1, 5, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 1, 1, 0, 1, 2, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map75 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 3, 1, 3, 1, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 0, 1, 2, 5, 2, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 2, 0, 4, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 1, 3, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map76 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 3, 0, 3, 0, 3, 2, 0, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 2, 1, 1, 1, 1, 2, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 3, 2, 3, 0, 3, 0, 4, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 2, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map77 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 3, 2, 3, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 1, 1, 3, 1, 1, 0, 4, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 2, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map78 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 4, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 1, 2, 2, 5, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 2, 2, 2, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 1, 1, 3, 1, 1, 0, 3, 0, 3, 0, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 3, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map79 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 3, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0, 2, 2, 1, 0, 1, 0, 0, 0},
            {0, 1, 0, 0, 3, 1, 3, 1, 5, 2, 1, 0, 1, 0, 0, 0},
            {0, 1, 0, 3, 3, 4, 0, 1, 2, 5, 1, 0, 1, 1, 1, 0},
            {0, 1, 0, 0, 3, 3, 0, 1, 2, 2, 1, 0, 0, 0, 1, 0},
            {0, 1, 1, 0, 0, 0, 0, 1, 2, 2, 3, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 1, 3, 1, 1, 2, 0, 1, 0, 1, 1, 1, 0},
            {0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map80 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 3, 0, 1, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 0, 3, 1, 0, 0, 1, 0, 0, 0},
            {0, 1, 2, 2, 2, 1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0},
            {0, 1, 2, 0, 0, 1, 0, 0, 3, 0, 1, 0, 0, 1, 0, 0},
            {0, 1, 2, 0, 0, 0, 0, 3, 0, 3, 0, 3, 0, 1, 0, 0},
            {0, 1, 2, 0, 0, 1, 0, 0, 3, 0, 1, 0, 0, 1, 0, 0},
            {0, 1, 2, 2, 2, 1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 4, 0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
}
class Maps2{  // 地图范围, [81-120]
    static int[][] map81 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1},
            {1, 2, 2, 0, 1, 1, 1, 1, 1, 0, 3, 0, 0, 1, 0, 1},
            {1, 2, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 1, 0, 1},
            {1, 2, 2, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0, 1},
            {1, 2, 2, 0, 1, 1, 0, 3, 0, 1, 3, 0, 3, 1, 0, 1},
            {1, 2, 2, 0, 1, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 1},
            {1, 2, 2, 0, 1, 0, 0, 3, 0, 1, 1, 1, 3, 0, 0, 1},
            {1, 2, 2, 0, 1, 0, 3, 0, 3, 0, 0, 3, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 0, 1, 0, 3, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 0, 1, 4, 1, 1, 0, 3, 0, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map82 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 4, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 3, 0, 3, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 5, 2, 5, 2, 5, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 2, 3, 0, 3, 2, 0, 1, 1, 0, 0, 0},
            {0, 1, 1, 1, 0, 5, 2, 5, 2, 5, 0, 1, 1, 1, 0, 0},
            {1, 1, 1, 1, 0, 0, 3, 0, 3, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map83 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 0, 4, 0, 5, 0, 5, 0, 5, 0, 1, 0, 0, 1, 1},
            {0, 1, 3, 1, 0, 0, 5, 0, 5, 0, 0, 1, 0, 0, 0, 1},
            {0, 1, 0, 1, 0, 5, 0, 5, 0, 5, 0, 0, 0, 0, 0, 1},
            {0, 1, 0, 1, 0, 0, 5, 0, 5, 0, 0, 1, 1, 0, 1, 1},
            {0, 1, 0, 1, 0, 5, 0, 5, 0, 5, 0, 1, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 0, 5, 0, 5, 0, 0, 1, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 5, 0, 5, 0, 5, 0, 1, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 0, 5, 0, 5, 0, 0, 1, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 5, 0, 2, 0, 5, 0, 1, 1, 0, 1, 1},
            {1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1},
            {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map84 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0},
            {0, 0, 1, 3, 3, 3, 0, 1, 0, 3, 3, 0, 0, 3, 1, 1},
            {0, 0, 1, 0, 3, 0, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1},
            {0, 0, 1, 0, 0, 3, 0, 0, 1, 3, 2, 1, 1, 2, 0, 1},
            {0, 0, 1, 0, 0, 1, 0, 3, 1, 0, 2, 2, 2, 2, 1, 1},
            {0, 1, 1, 3, 0, 3, 0, 0, 1, 3, 2, 1, 1, 2, 0, 1},
            {0, 1, 0, 3, 0, 0, 3, 0, 4, 3, 2, 2, 2, 2, 0, 1},
            {0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map85 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 1, 3, 1, 0, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 3, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 3, 0, 0, 3, 0, 0, 1, 0},
            {0, 1, 1, 0, 3, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0},
            {0, 1, 0, 0, 0, 1, 3, 1, 1, 1, 1, 0, 0, 0, 1, 0},
            {0, 1, 0, 0, 0, 0, 3, 0, 1, 1, 1, 0, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 2, 2, 0, 4, 1, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 3, 0, 3, 3, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map86 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 0},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 2, 2, 2, 2, 1, 0},
            {1, 0, 3, 0, 0, 0, 3, 3, 0, 1, 5, 5, 5, 2, 1, 0},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 2, 2, 5, 2, 1, 0},
            {1, 0, 0, 3, 0, 3, 0, 3, 0, 1, 5, 2, 5, 2, 1, 0},
            {1, 1, 0, 3, 0, 3, 0, 3, 0, 2, 5, 2, 5, 2, 1, 1},
            {1, 0, 0, 3, 0, 3, 0, 3, 0, 2, 5, 2, 5, 2, 4, 1},
            {1, 0, 0, 3, 0, 3, 0, 3, 0, 1, 5, 2, 5, 2, 1, 1},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 2, 2, 5, 2, 1, 0},
            {1, 0, 3, 0, 0, 0, 3, 3, 0, 1, 5, 5, 5, 2, 1, 0},
            {1, 0, 3, 0, 3, 0, 3, 0, 0, 1, 2, 2, 2, 2, 1, 0},
            {1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 0},
            {1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map87 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 1, 1, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 0, 0, 3, 1, 2, 2, 0, 0, 1, 1, 1, 0, 0, 0, 0},
            {1, 1, 0, 0, 2, 2, 2, 1, 3, 3, 0, 1, 1, 1, 1, 1},
            {0, 1, 0, 3, 2, 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 1},
            {0, 1, 0, 3, 1, 1, 1, 3, 1, 1, 0, 1, 0, 3, 0, 1},
            {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, 3, 1, 0, 1},
            {0, 1, 1, 3, 3, 1, 0, 1, 1, 3, 1, 3, 0, 0, 0, 1},
            {0, 1, 2, 2, 2, 0, 3, 4, 0, 3, 0, 0, 0, 1, 1, 1},
            {0, 1, 2, 2, 2, 1, 3, 1, 0, 0, 0, 1, 1, 1, 0, 0},
            {0, 1, 2, 2, 2, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map88 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 5, 0, 0, 0, 5, 0, 5, 5, 0, 0, 1, 0},
            {0, 0, 1, 0, 3, 0, 5, 0, 0, 0, 0, 5, 1, 0, 1, 0},
            {0, 0, 1, 0, 2, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 4, 1, 1, 0},
            {0, 0, 1, 0, 5, 0, 2, 0, 5, 0, 0, 5, 5, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 5, 0, 0, 0, 5, 0, 1, 3, 1, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map89 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 3, 3, 3, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 3, 0, 3, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 1, 2, 2, 2, 2, 2, 0, 1, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 2, 2, 2, 2, 2, 0, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map90 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 3, 0, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 2, 2, 5, 2, 0, 0, 3, 0, 1, 0},
            {1, 0, 0, 3, 4, 3, 2, 2, 2, 2, 1, 3, 3, 0, 1, 0},
            {1, 0, 0, 0, 3, 0, 1, 3, 1, 1, 1, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map91 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 3, 0, 2, 2, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 3, 0, 2, 2, 3, 3, 4, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 2, 2, 0, 3, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map92 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 1, 1, 3, 1, 2, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 3, 0, 2, 2, 2, 1, 0, 3, 0, 1, 0, 0},
            {0, 0, 1, 4, 0, 3, 2, 1, 5, 3, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map93 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 3, 0, 4, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 3, 0, 1, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {1, 1, 3, 0, 1, 1, 2, 1, 1, 0, 0, 1, 1, 1, 1, 0},
            {0, 1, 0, 0, 2, 2, 2, 2, 2, 0, 3, 1, 0, 0, 1, 0},
            {0, 1, 0, 3, 1, 1, 2, 1, 1, 0, 0, 1, 3, 0, 1, 0},
            {0, 1, 0, 0, 0, 1, 2, 1, 1, 0, 0, 0, 0, 0, 1, 0},
            {0, 1, 1, 1, 0, 3, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0},
            {0, 0, 0, 1, 0, 1, 3, 0, 0, 0, 0, 0, 3, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map94 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 5, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 4, 2, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 2, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 5, 2, 5, 5, 5, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 5, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 3, 5, 3, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map95 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0},
            {1, 1, 1, 0, 1, 3, 0, 0, 3, 0, 1, 1, 1, 1, 0, 0},
            {1, 0, 0, 3, 0, 0, 1, 1, 2, 2, 1, 0, 0, 1, 1, 1},
            {1, 0, 1, 0, 0, 3, 0, 1, 2, 2, 3, 0, 3, 0, 0, 1},
            {1, 0, 3, 3, 0, 0, 3, 1, 2, 2, 0, 0, 1, 0, 0, 1},
            {1, 1, 0, 0, 1, 0, 0, 2, 2, 2, 1, 3, 0, 0, 0, 1},
            {0, 1, 3, 0, 4, 0, 1, 2, 2, 2, 1, 0, 3, 0, 0, 1},
            {0, 1, 0, 0, 0, 0, 1, 2, 2, 2, 3, 0, 0, 0, 1, 1},
            {0, 1, 0, 1, 1, 3, 0, 1, 1, 1, 0, 0, 0, 1, 1, 0},
            {0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
            {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map96 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 0, 2, 2, 2, 2, 2, 1, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 1, 2, 2, 2, 2, 2, 2, 0, 1, 1, 0, 0},
            {0, 1, 1, 0, 1, 1, 1, 1, 3, 1, 1, 3, 1, 0, 0, 0},
            {0, 1, 4, 3, 0, 0, 3, 0, 3, 0, 0, 0, 1, 1, 1, 0},
            {0, 1, 0, 3, 3, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0},
            {0, 1, 0, 1, 0, 0, 3, 3, 1, 1, 0, 0, 1, 0, 1, 0},
            {0, 1, 0, 0, 0, 3, 0, 0, 1, 0, 3, 3, 0, 0, 1, 0},
            {0, 1, 0, 0, 3, 0, 0, 3, 0, 0, 0, 1, 3, 0, 1, 0},
            {0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 3, 0, 3, 0, 1, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map97 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 3, 0, 3, 3, 3, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 3, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 4, 3, 0, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 3, 0, 3, 0, 1, 0, 1, 1, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 3, 1, 3, 1, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {1, 2, 2, 2, 0, 0, 1, 0, 3, 1, 0, 1, 1, 1, 0, 0},
            {1, 2, 2, 2, 2, 2, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
            {1, 2, 2, 2, 2, 2, 1, 0, 3, 1, 0, 1, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map98 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0},
            {0, 1, 1, 0, 3, 0, 3, 0, 1, 3, 0, 1, 3, 1, 0, 0},
            {0, 1, 0, 0, 3, 0, 4, 0, 3, 0, 0, 3, 0, 1, 1, 0},
            {0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 2, 2, 2, 2, 1, 0},
            {0, 1, 0, 0, 1, 1, 0, 3, 0, 1, 2, 1, 1, 2, 1, 0},
            {0, 1, 1, 0, 0, 3, 0, 0, 0, 0, 2, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 3, 3, 0, 1, 3, 1, 2, 2, 2, 2, 1, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 3, 0, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 3, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map99 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {1, 2, 2, 1, 0, 0, 0, 0, 3, 3, 1, 0, 1, 0, 0, 0},
            {1, 2, 2, 0, 0, 1, 1, 0, 0, 0, 3, 0, 1, 1, 1, 0},
            {1, 2, 2, 1, 0, 0, 1, 1, 3, 1, 0, 3, 0, 0, 1, 0},
            {1, 2, 2, 0, 0, 0, 1, 0, 4, 3, 0, 3, 0, 0, 1, 0},
            {1, 2, 2, 1, 0, 0, 1, 0, 3, 0, 3, 0, 0, 0, 1, 0},
            {1, 0, 2, 0, 0, 0, 1, 0, 3, 0, 3, 0, 1, 1, 1, 0},
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] map100 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 1, 1, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 3, 0, 1},
            {1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1},
            {0, 1, 0, 1, 0, 0, 1, 0, 3, 0, 3, 0, 0, 3, 0, 1},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 0, 0, 1},
            {0, 0, 1, 0, 0, 3, 0, 3, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 4, 2, 2, 3, 0, 5, 5, 2, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
