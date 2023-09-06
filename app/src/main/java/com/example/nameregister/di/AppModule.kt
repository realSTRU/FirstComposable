
package com.example.nameregister.di
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.nameregister.PersonaDb

@Module
@InstallIn ( SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTicketDatabase(@ApplicationContext appContext: Context): PersonaDb =
        Room.databaseBuilder(
            appContext,
            PersonaDb::class.java,
            "Persona.db")
            .fallbackToDestructiveMigration()
            .build()
}