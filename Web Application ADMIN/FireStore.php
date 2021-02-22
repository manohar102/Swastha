<?php
require_once 'vendor/autoload.php';
use Google\Cloud\Firestore\FirestoreClient;
class FireStore
{

    protected $db;
    protected $name;
    public function __construct(string $collection)
    {
        $this->db = new FirestoreClient([
            'projectID' => 'swashta-18661',
            'keyFile' => json_decode(file_get_contents('key.json'), true)

        ]);
        $this->name = $collection;
    }

    /**
     * @return FirestoreClient
     */
    public function getDocument(string $name)
    {
        return $this->db->collection($this->name)->document($name)->snapshot()->data();

    }
    public function getCollection(string $name){
        $citiesRef = $this->db->collection('users');
        $documents = $citiesRef->documents();
        foreach ($documents as $document) {
            if ($document->exists()) {
                printf("<tr>");
                printf("<td >".$document->id()."</td>");
                printf("<td >".$document->data()['deviceName']."</td>");
                printf("<td >".$document->data()['cholestrol']."</td>");
                printf("<td >".$document->data()['bmi']."</td>");
                printf("</tr>");
                //print_r($document->data());
                //printf(PHP_EOL);
            } else {
                printf('Document %s does not exist!' . PHP_EOL, $snapshot->id());
            }
        }
    }
    public function getCollection2(string $name){
        $citiesRef = $this->db->collection('users');
        $documents = $citiesRef->documents();
        foreach ($documents as $document) {
            if ($document->exists()) {
                printf("<tr>");
                printf("<td >".$document->id()."</td>");
                printf('<td  class="td-actions text-right">
                            <button type="button" rel="tooltip" class="btn btn-success btn-round">
                                <i class="material-icons">get_app</i>
                            </button>
                        </td>');
                printf("</tr>");
                //print_r($document->data());
                //printf(PHP_EOL);
            } else {
                printf('Document %s does not exist!' . PHP_EOL, $snapshot->id());
            }
        }
    }
    /**
     * @return string
     */
    public function newCollection(string $user, string $name,string $doc_name, array $data=[])
    {
        try {
            $this->db->collection('users')->document($user)->collection($name)->document($doc_name)->create($data);
            return true;
        }catch (Exception $exception){
            return $exception->getMessage();
        }
    }
}