function gen_parser {
    kotlinc -script tools/gen_parser/gen_parser.kts
}

function server_upload {
    ./gradlew :server:clean :server:dist
    scp server/build/libs/server-1.0.jar root@121.201.68.222:~/
}

function pack_textures {
    java -cp tools/pack_textures/runnable-texturepacker.jar com.badlogic.gdx.tools.texturepacker.TexturePacker assets_prepack assets atlas
}