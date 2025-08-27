package idl.SmartDemo;

import com.zrdds.infrastructure.*;
import com.zrdds.topic.TypeSupport;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.DataReader;
import java.io.UnsupportedEncodingException;

public class CommandTypeSupport extends TypeSupport {
    private String type_name = "Command";
    private static TypeCodeImpl s_typeCode = null;
    private static CommandTypeSupport m_instance = new CommandTypeSupport();

    private final byte[] tmp_byte_obj = new byte[1];
    private final char[] tmp_char_obj = new char[1];
    private final short[] tmp_short_obj = new short[1];
    private final int[] tmp_int_obj = new int[1];
    private final long[] tmp_long_obj = new long[1];
    private final float[] tmp_float_obj = new float[1];
    private final double[] tmp_double_obj = new double[1];
    private final boolean[] tmp_boolean_obj = new boolean[1];

    
    private CommandTypeSupport(){}

    
    public static TypeSupport get_instance() { return m_instance; }

    public Object create_sampleI() {
        Command sample = new Command();
        return sample;
    }

    public void destroy_sampleI(Object sample) {

    }

    public int copy_sampleI(Object dst,Object src) {
        Command CommandDst = (Command)dst;
        Command CommandSrc = (Command)src;
        CommandDst.copy(CommandSrc);
        return 1;
    }

    public int print_sample(Object _sample) {
        if (_sample == null){
            System.out.println("NULL");
            return -1;
        }
        Command sample = (Command)_sample;
        if (sample.device != null){
            System.out.println("sample.device:" + sample.device);
        }
        else{
            System.out.println("sample.device: null");
        }
        System.out.println("sample.action:" + sample.action);
        System.out.println("sample.value:" + sample.value);
        System.out.println("sample.autoMode:" + sample.autoMode);
        System.out.println("sample.timestamp_ms:" + sample.timestamp_ms);
        return 0;
    }

    public String get_type_name(){
        return this.type_name;
    }

    public int get_max_sizeI(){
        return 276;
    }

    public int get_max_key_sizeI(){
        return 276;
    }

    public boolean has_keyI(){
        return false;
    }

    public String get_keyhashI(Object sample, long cdr){
        return "-1";
    }

    public DataReader create_data_reader() {return new CommandDataReader();}

    public DataWriter create_data_writer() {return new CommandDataWriter();}

    public TypeCode get_inner_typecode(){
        TypeCode userTypeCode = get_typecode();
        if (userTypeCode == null) return null;
        return userTypeCode.get_impl();
    }

    public int get_sizeI(Object _sample,long cdr, int offset) throws UnsupportedEncodingException {
        int initialAlignment = offset;
        Command sample = (Command)_sample;
        offset += CDRSerializer.get_string_size(sample.device == null ? 0 : sample.device.getBytes().length, offset);

        offset += CDRSerializer.get_untype_size(2, offset);

        offset += CDRSerializer.get_untype_size(4, offset);

        offset += CDRSerializer.get_untype_size(1, offset);

        offset += CDRSerializer.get_untype_size(4, offset);

        return offset - initialAlignment;
    }

    public int serializeI(Object _sample ,long cdr) {
         Command sample = (Command) _sample;

        if (!CDRSerializer.put_string(cdr, sample.device, sample.device == null ? 0 : sample.device.length())){
            System.out.println("serialize sample.device failed.");
            return -2;
        }

        if (!CDRSerializer.put_short(cdr, sample.action)){
            System.out.println("serialize sample.action failed.");
            return -2;
        }

        if (!CDRSerializer.put_float(cdr, sample.value)){
            System.out.println("serialize sample.value failed.");
            return -2;
        }

        if (!CDRSerializer.put_boolean(cdr, sample.autoMode)){
            System.out.println("serialize sample.autoMode failed.");
            return -2;
        }

        if (!CDRSerializer.put_int(cdr, sample.timestamp_ms)){
            System.out.println("serialize sample.timestamp_ms failed.");
            return -2;
        }

        return 0;
    }

    synchronized public int deserializeI(Object _sample, long cdr){
        Command sample = (Command) _sample;
        sample.device = CDRDeserializer.get_string(cdr);
        if(sample.device ==null){
            System.out.println("deserialize member sample.device failed.");
            return -3;
        }

        if (!CDRDeserializer.get_short_array(cdr, tmp_short_obj, 1)){
            System.out.println("deserialize sample.action failed.");
            return -2;
        }
        sample.action= tmp_short_obj[0];

        if (!CDRDeserializer.get_float_array(cdr, tmp_float_obj, 1)){
            System.out.println("deserialize sample.value failed.");
            return -2;
        }
        sample.value= tmp_float_obj[0];

        if (!CDRDeserializer.get_boolean_array(cdr, tmp_boolean_obj, 1)){
            System.out.println("deserialize sample.autoMode failed.");
            return -2;
        }
        sample.autoMode= tmp_boolean_obj[0];

        if (!CDRDeserializer.get_int_array(cdr, tmp_int_obj, 1)){
            System.out.println("deserialize sample.timestamp_ms failed.");
            return -2;
        }
        sample.timestamp_ms= tmp_int_obj[0];

        return 0;
    }

    public int get_key_sizeI(Object _sample,long cdr,int offset)throws UnsupportedEncodingException {
        int initialAlignment = offset;
        Command sample = (Command)_sample;
        offset += get_sizeI(sample, cdr, offset);
        return offset - initialAlignment;
    }

    public int serialize_keyI(Object _sample, long cdr){
        Command sample = (Command)_sample;
        return 0;
    }

    public int deserialize_keyI(Object _sample, long cdr) {
        Command sample = (Command)_sample;
        return 0;
    }

    public TypeCode get_typecode(){
        if (s_typeCode != null) {
            return s_typeCode;
        }
        TypeCodeFactory factory = TypeCodeFactory.get_instance();

        s_typeCode = factory.create_struct_TC("SmartDemo.Command");
        if (s_typeCode == null){
            System.out.println("create struct Command typecode failed.");
            return s_typeCode;
        }
        int ret = 0;
        TypeCodeImpl memberTc = new TypeCodeImpl();
        TypeCodeImpl eleTc = new TypeCodeImpl();

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member device TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            0,
            0,
            "device",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.get_primitive_TC(TypeCodeKind.DDS_TK_SHORT);
        if (memberTc == null){
            System.out.println("Get Member action TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            1,
            1,
            "action",
            memberTc,
            false,
            false);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.get_primitive_TC(TypeCodeKind.DDS_TK_FLOAT);
        if (memberTc == null){
            System.out.println("Get Member value TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            2,
            2,
            "value",
            memberTc,
            false,
            false);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.get_primitive_TC(TypeCodeKind.DDS_TK_BOOLEAN);
        if (memberTc == null){
            System.out.println("Get Member autoMode TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            3,
            3,
            "autoMode",
            memberTc,
            false,
            false);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.get_primitive_TC(TypeCodeKind.DDS_TK_INT);
        if (memberTc == null){
            System.out.println("Get Member timestamp_ms TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            4,
            4,
            "timestamp_ms",
            memberTc,
            false,
            false);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        return s_typeCode;
    }

}