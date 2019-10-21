package com.uber.cursoandroid.manoelprado.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uber.cursoandroid.manoelprado.uber.R;
import com.uber.cursoandroid.manoelprado.uber.config.ConfiguracaoFirebase;
import com.uber.cursoandroid.manoelprado.uber.helper.UsuarioFirebase;
import com.uber.cursoandroid.manoelprado.uber.model.Destino;
import com.uber.cursoandroid.manoelprado.uber.model.Requisicao;
import com.uber.cursoandroid.manoelprado.uber.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity
        implements OnMapReadyCallback { //precisa implementar o OnMapReadyCallback

    //em Activity_passageiro vai ser mostrado a interface para perdir o uber
    // ja em content_passageiro vai aparecer o MAPA!

    //Componentes
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button botaoChamarUber;

    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean uberChamado = false;
    private DatabaseReference fireBaseRef;
    private Requisicao requisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        inicializarComponentes();

        //Adicionar LISTENER para status da requisição
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = fireBaseRef.child("requisicoes");
        final Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id") //odernando pelo acesesso, primerio passageiro e em seguida id
            .equalTo(usuarioLogado.getId());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()){ //para passar por todas as requisicoes do usuarioLogado
                    lista.add(ds.getValue(Requisicao.class)); //adicionamos cada requisicao na lista
                }

                Collections.reverse(lista); //inverteu a ordem

                if (lista!=null && lista.size()>0){
                    requisicao = lista.get(0); //pega a requisicao mais recente
                    //Log.d("resultado", "onDataChange: "+requisicao.getId());
                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO :
                            linearLayoutDestino.setVisibility(View.GONE);
                            botaoChamarUber.setText("Cancelar Uber");
                            uberChamado = true;
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Recuperar Localização do Usuário
        recuperarLocalizacaoDoUsuario();
    }

    public void chamarUber (View view){

        if (!uberChamado){ //Uber não chamado !false=true
            //Início
            String enderecoDestino = editDestino.getText().toString();

            if((!enderecoDestino.equals("")) || (enderecoDestino!=null)){
                //se NAO for vazio ou diferente de null

                Address adressDestino = recuperarEndereco (enderecoDestino);

                if (adressDestino!=null){
                    final Destino destino = new Destino();
                    destino.setCidade(adressDestino.getSubAdminArea());
                    //destino.setCidade(adressDestino.getAdminArea());  //esse é para ESTADO
                    destino.setCep(adressDestino.getPostalCode());
                    destino.setBairro(adressDestino.getSubLocality());
                    destino.setRua(adressDestino.getThoroughfare());
                    destino.setNumero(adressDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(adressDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(adressDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Cidade: "+destino.getCidade()); //vai adicionando strings em uma unica string
                    mensagem.append("\nRua: "+destino.getRua());
                    mensagem.append("\nBairro: "+destino.getBairro());
                    mensagem.append("\nNúmero: "+destino.getNumero());
                    mensagem.append("\nCep: "+destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço de Destino")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Salvar Requisição
                                    salvarRequisicao(destino);
                                    uberChamado = true; //mudamos para true, para se clicar ele cai no else (cancela)
                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog dialog = builder .create();
                    dialog.show();
                }

            }else {
                //se for vazio
                Toast.makeText(PassageiroActivity.this, "Informe o endereço de destino!", Toast.LENGTH_SHORT).show();
            }
            //fim
        }else { //uber já foi chamado
            //Pode cancelar a requisicao

            uberChamado = false;
        }


    }

    private void salvarRequisicao(Destino destino){

        //1 momento (passageiro chamando o motorista)
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado(); //usuarioPassageiroLOGADO
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));

        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        botaoChamarUber.setText("Cancelar Uber");

    }

    private Address recuperarEndereco(String endereco){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if (listaEnderecos != null && listaEnderecos.size()>0){
                Address address = listaEnderecos.get(0);

                /*double lat = address.getLatitude();
                double lon = address.getLongitude();*/

                return address;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

        private void recuperarLocalizacaoDoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar a lat e lng
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localPassageiro)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );

                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localPassageiro, 17)
                );

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  //nos criamos um menu_main!
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar uma viagem");
        setSupportActionBar(toolbar);

        //Inicializar componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        botaoChamarUber = findViewById(R.id.botaoChamarUber);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        fireBaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //inicializa o mapa
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}
