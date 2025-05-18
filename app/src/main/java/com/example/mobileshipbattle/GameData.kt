package com.example.mobileshipbattle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object GameData {
    private var _gameModel : MutableLiveData<GameModel> = MutableLiveData()
    var gameModel : LiveData<GameModel> = _gameModel
    var myID = ""


    fun saveGameModel(model: GameModel) {
        _gameModel.postValue(model)

        if (model.gameId != "-1") {
            Firebase.firestore.collection("games")
                .document(model.gameId)
                .set(model)
                .addOnSuccessListener {
                    // Data successfully written
                }
                .addOnFailureListener {
                    // Handle errors
                }
        }
    }

    fun fetchGameModel() {
        gameModel.value?.apply {
            if (gameId != "-1") {
                Firebase.firestore.collection("games")
                    .document(gameId)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }

                        val model = value?.toObject(GameModel::class.java)
                        if (model != null) {
                            _gameModel.postValue(model)
                        }
                    }
            }
        }
    }
}